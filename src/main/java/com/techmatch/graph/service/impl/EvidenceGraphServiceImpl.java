package com.techmatch.graph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.output.InterviewQuestionOutput;
import com.techmatch.agent.output.RejectedClaim;
import com.techmatch.agent.output.RiskOutput;
import com.techmatch.agent.output.VerifiedClaim;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.graph.dto.EvidenceGraphChainResponse;
import com.techmatch.graph.dto.EvidenceGraphEdgeResponse;
import com.techmatch.graph.dto.EvidenceGraphNodeResponse;
import com.techmatch.graph.dto.EvidenceGraphResponse;
import com.techmatch.graph.entity.EvidenceEdgeEntity;
import com.techmatch.graph.entity.EvidenceNodeEntity;
import com.techmatch.graph.mapper.EvidenceEdgeMapper;
import com.techmatch.graph.mapper.EvidenceNodeMapper;
import com.techmatch.graph.service.EvidenceGraphService;
import com.techmatch.task.service.AgentTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvidenceGraphServiceImpl implements EvidenceGraphService {

    private final EvidenceNodeMapper evidenceNodeMapper;
    private final EvidenceEdgeMapper evidenceEdgeMapper;
    private final AgentTaskService agentTaskService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void rebuild(MatchAgentContext context) {
        Long taskId = context.getTaskId();
        Long userId = context.getUserId();

        evidenceEdgeMapper.delete(new LambdaQueryWrapper<EvidenceEdgeEntity>()
                .eq(EvidenceEdgeEntity::getTaskId, taskId));
        evidenceNodeMapper.delete(new LambdaQueryWrapper<EvidenceNodeEntity>()
                .eq(EvidenceNodeEntity::getTaskId, taskId));

        Map<String, EvidenceNodeEntity> jdNodes = new LinkedHashMap<>();
        Map<String, EvidenceNodeEntity> riskNodes = new LinkedHashMap<>();

        for (VerifiedClaim claim : context.getEvidenceVerificationOutput().getVerifiedClaims()) {
            if (!StringUtils.hasText(claim.getRequirement())) {
                continue;
            }

            EvidenceNodeEntity jdNode = jdNodes.computeIfAbsent(claim.getRequirement(), requirement ->
                    createNode(taskId, userId, "JD_REQUIREMENT", "jd:" + requirement, requirement, requirement, null, null));

            EvidenceNodeEntity evidenceNode = createNode(
                    taskId,
                    userId,
                    "RESUME_EVIDENCE",
                    "evidence:" + claim.getRequirement() + ":" + claim.getClaim(),
                    claim.getClaim(),
                    claim.getEvidence(),
                    claim.getStatus(),
                    Map.of("claimType", claim.getClaimType())
            );

            createEdge(
                    taskId,
                    jdNode.getId(),
                    evidenceNode.getId(),
                    "PARTIAL".equals(claim.getStatus()) ? "PARTIALLY_MATCHED_BY" : "MATCHED_BY",
                    Map.of("status", claim.getStatus())
            );
        }

        for (RiskOutput risk : context.getEvidenceVerificationOutput().getRisks()) {
            String requirement = StringUtils.hasText(risk.getRequirement()) ? risk.getRequirement() : risk.getTitle();
            EvidenceNodeEntity riskNode = riskNodes.computeIfAbsent(risk.getTitle() + "|" + requirement, key ->
                    createNode(taskId, userId, "RISK", "risk:" + key, risk.getTitle(), risk.getDetail(), risk.getSeverity(), null));

            if (StringUtils.hasText(risk.getRequirement())) {
                EvidenceNodeEntity jdNode = jdNodes.computeIfAbsent(risk.getRequirement(), value ->
                        createNode(taskId, userId, "JD_REQUIREMENT", "jd:" + value, value, value, null, null));
                createEdge(taskId, jdNode.getId(), riskNode.getId(), "GENERATES_RISK", Map.of("severity", risk.getSeverity()));
                if ("缺失证据".equals(risk.getTitle())) {
                    createEdge(taskId, jdNode.getId(), riskNode.getId(), "MISSING_EVIDENCE", Map.of("severity", risk.getSeverity()));
                }
            }
        }

        for (RejectedClaim claim : context.getEvidenceVerificationOutput().getRejectedClaims()) {
            if (!StringUtils.hasText(claim.getRequirement())) {
                continue;
            }
            EvidenceNodeEntity jdNode = jdNodes.computeIfAbsent(claim.getRequirement(), requirement ->
                    createNode(taskId, userId, "JD_REQUIREMENT", "jd:" + requirement, requirement, requirement, null, null));
            EvidenceNodeEntity riskNode = riskNodes.computeIfAbsent("缺失证据|" + claim.getRequirement(), key ->
                    createNode(taskId, userId, "RISK", "risk:" + key, "缺失证据", claim.getReason(), "HIGH", null));
            createEdge(taskId, jdNode.getId(), riskNode.getId(), "MISSING_EVIDENCE", Map.of("reason", claim.getReason()));
        }

        for (InterviewQuestionOutput question : context.getInterviewQuestions()) {
            EvidenceNodeEntity questionNode = createNode(
                    taskId,
                    userId,
                    "INTERVIEW_QUESTION",
                    "question:" + question.getTarget() + ":" + question.getQuestion(),
                    question.getTarget(),
                    question.getQuestion(),
                    question.getDifficulty(),
                    Map.of("type", question.getType())
            );

            EvidenceNodeEntity riskNode = findRelatedRiskNode(riskNodes, question.getTarget());
            if (riskNode != null) {
                createEdge(taskId, riskNode.getId(), questionNode.getId(), "GENERATES_QUESTION", Map.of("type", question.getType()));
                continue;
            }

            EvidenceNodeEntity jdNode = jdNodes.get(question.getTarget());
            if (jdNode != null) {
                createEdge(taskId, jdNode.getId(), questionNode.getId(), "GENERATES_QUESTION", Map.of("type", question.getType()));
            }
        }
    }

    @Override
    public EvidenceGraphResponse getGraph(Long taskId, Long userId) {
        agentTaskService.requireOwnedTask(taskId, userId);

        List<EvidenceNodeEntity> nodes = evidenceNodeMapper.selectList(new LambdaQueryWrapper<EvidenceNodeEntity>()
                .eq(EvidenceNodeEntity::getTaskId, taskId)
                .eq(EvidenceNodeEntity::getUserId, userId)
                .orderByAsc(EvidenceNodeEntity::getId));
        List<EvidenceEdgeEntity> edges = evidenceEdgeMapper.selectList(new LambdaQueryWrapper<EvidenceEdgeEntity>()
                .eq(EvidenceEdgeEntity::getTaskId, taskId)
                .orderByAsc(EvidenceEdgeEntity::getId));

        Map<Long, EvidenceNodeEntity> nodeMap = new LinkedHashMap<>();
        nodes.forEach(node -> nodeMap.put(node.getId(), node));

        return EvidenceGraphResponse.builder()
                .taskId(taskId)
                .nodes(nodes.stream().map(this::toNodeResponse).toList())
                .edges(edges.stream().map(this::toEdgeResponse).toList())
                .chains(buildChains(nodes, edges, nodeMap))
                .build();
    }

    private List<EvidenceGraphChainResponse> buildChains(List<EvidenceNodeEntity> nodes,
                                                         List<EvidenceEdgeEntity> edges,
                                                         Map<Long, EvidenceNodeEntity> nodeMap) {
        List<EvidenceGraphChainResponse> chains = new ArrayList<>();
        List<EvidenceNodeEntity> jdNodes = nodes.stream()
                .filter(node -> "JD_REQUIREMENT".equals(node.getNodeType()))
                .toList();

        for (EvidenceNodeEntity jdNode : jdNodes) {
            String evidence = null;
            String status = null;
            String risk = null;
            String question = null;

            for (EvidenceEdgeEntity edge : edges) {
                if (!Objects.equals(edge.getFromNodeId(), jdNode.getId())) {
                    continue;
                }
                EvidenceNodeEntity toNode = nodeMap.get(edge.getToNodeId());
                if (toNode == null) {
                    continue;
                }

                switch (edge.getEdgeType()) {
                    case "MATCHED_BY", "PARTIALLY_MATCHED_BY" -> {
                        evidence = toNode.getContent();
                        status = "MATCHED_BY".equals(edge.getEdgeType()) ? "强匹配" : "部分匹配";
                    }
                    case "GENERATES_RISK", "MISSING_EVIDENCE" -> {
                        risk = toNode.getContent();
                        if (status == null) {
                            status = "MISSING_EVIDENCE".equals(edge.getEdgeType()) ? "缺失证据" : "存在风险";
                        }
                        String linkedQuestion = findQuestionForRisk(toNode.getId(), edges, nodeMap);
                        if (linkedQuestion != null) {
                            question = linkedQuestion;
                        }
                    }
                    case "GENERATES_QUESTION" -> question = toNode.getContent();
                    default -> {
                    }
                }
            }

            chains.add(EvidenceGraphChainResponse.builder()
                    .requirement(jdNode.getTitle())
                    .evidence(evidence)
                    .status(status == null ? "待核验" : status)
                    .risk(risk)
                    .question(question)
                    .build());
        }

        return chains;
    }

    private String findQuestionForRisk(Long riskNodeId,
                                       List<EvidenceEdgeEntity> edges,
                                       Map<Long, EvidenceNodeEntity> nodeMap) {
        return edges.stream()
                .filter(edge -> Objects.equals(edge.getFromNodeId(), riskNodeId))
                .filter(edge -> "GENERATES_QUESTION".equals(edge.getEdgeType()))
                .map(edge -> nodeMap.get(edge.getToNodeId()))
                .filter(Objects::nonNull)
                .map(EvidenceNodeEntity::getContent)
                .findFirst()
                .orElse(null);
    }

    private EvidenceNodeEntity findRelatedRiskNode(Map<String, EvidenceNodeEntity> riskNodes, String target) {
        return riskNodes.values().stream()
                .filter(node -> StringUtils.hasText(target) && (target.equals(node.getTitle()) || target.equals(node.getNodeKey().replace("risk:", ""))))
                .findFirst()
                .orElseGet(() -> riskNodes.values().stream()
                        .filter(node -> StringUtils.hasText(target) && StringUtils.hasText(node.getContent()) && node.getContent().contains(target))
                        .findFirst()
                        .orElse(null));
    }

    private EvidenceNodeEntity createNode(Long taskId,
                                          Long userId,
                                          String nodeType,
                                          String nodeKey,
                                          String title,
                                          String content,
                                          String status,
                                          Map<String, Object> metadata) {
        EvidenceNodeEntity entity = new EvidenceNodeEntity();
        entity.setTaskId(taskId);
        entity.setUserId(userId);
        entity.setNodeType(nodeType);
        entity.setNodeKey(nodeKey);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setStatus(status);
        entity.setMetadataJson(writeJson(metadata));
        entity.setCreateTime(LocalDateTime.now());
        evidenceNodeMapper.insert(entity);
        return entity;
    }

    private void createEdge(Long taskId,
                            Long fromNodeId,
                            Long toNodeId,
                            String edgeType,
                            Map<String, Object> metadata) {
        EvidenceEdgeEntity entity = new EvidenceEdgeEntity();
        entity.setTaskId(taskId);
        entity.setFromNodeId(fromNodeId);
        entity.setToNodeId(toNodeId);
        entity.setEdgeType(edgeType);
        entity.setMetadataJson(writeJson(metadata));
        entity.setCreateTime(LocalDateTime.now());
        evidenceEdgeMapper.insert(entity);
    }

    private EvidenceGraphNodeResponse toNodeResponse(EvidenceNodeEntity node) {
        return EvidenceGraphNodeResponse.builder()
                .nodeId(node.getId())
                .nodeType(node.getNodeType())
                .nodeKey(node.getNodeKey())
                .title(node.getTitle())
                .content(node.getContent())
                .status(node.getStatus())
                .metadataJson(node.getMetadataJson())
                .build();
    }

    private EvidenceGraphEdgeResponse toEdgeResponse(EvidenceEdgeEntity edge) {
        return EvidenceGraphEdgeResponse.builder()
                .edgeId(edge.getId())
                .fromNodeId(edge.getFromNodeId())
                .toNodeId(edge.getToNodeId())
                .edgeType(edge.getEdgeType())
                .metadataJson(edge.getMetadataJson())
                .build();
    }

    private String writeJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), exception.getMessage());
        }
    }
}
