package com.techmatch.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.agent.output.FinalReportOutput;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.task.dto.AgentTaskResponse;
import com.techmatch.task.entity.AgentTaskEntity;
import com.techmatch.task.mapper.AgentTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgentTaskService {

    public static final String TASK_TYPE_MATCH = "MATCH_ANALYSIS";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    private final AgentTaskMapper agentTaskMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public AgentTaskEntity createMatchTask(Long userId, Long resumeId, Long jobDescriptionId) {
        LocalDateTime now = LocalDateTime.now();

        AgentTaskEntity entity = new AgentTaskEntity();
        entity.setUserId(userId);
        entity.setResumeId(resumeId);
        entity.setJobDescriptionId(jobDescriptionId);
        entity.setTaskType(TASK_TYPE_MATCH);
        entity.setStatus(STATUS_PENDING);
        entity.setCurrentNode("QUEUED");
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        agentTaskMapper.insert(entity);
        return entity;
    }

    @Transactional
    public void markRunning(Long taskId, String currentNode) {
        AgentTaskEntity entity = requireById(taskId);
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_RUNNING);
        entity.setCurrentNode(currentNode);
        if (entity.getStartedAt() == null) {
            entity.setStartedAt(now);
        }
        entity.setUpdateTime(now);
        agentTaskMapper.updateById(entity);
    }

    @Transactional
    public void markCompleted(Long taskId, String currentNode) {
        AgentTaskEntity entity = requireById(taskId);
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_COMPLETED);
        entity.setCurrentNode(currentNode);
        if (entity.getStartedAt() == null) {
            entity.setStartedAt(now);
        }
        entity.setFinishedAt(now);
        entity.setUpdateTime(now);
        agentTaskMapper.updateById(entity);
    }

    @Transactional
    public void markFailed(Long taskId, String currentNode, String errorMessage) {
        AgentTaskEntity entity = requireById(taskId);
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_FAILED);
        entity.setCurrentNode(currentNode);
        entity.setErrorMessage(errorMessage);
        if (entity.getStartedAt() == null) {
            entity.setStartedAt(now);
        }
        entity.setFinishedAt(now);
        entity.setUpdateTime(now);
        agentTaskMapper.updateById(entity);
    }

    public AgentTaskEntity requireOwnedTask(Long taskId, Long userId) {
        AgentTaskEntity entity = agentTaskMapper.selectOne(new LambdaQueryWrapper<AgentTaskEntity>()
                .eq(AgentTaskEntity::getId, taskId)
                .eq(AgentTaskEntity::getUserId, userId)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.AGENT_TASK_NOT_FOUND);
        }
        return entity;
    }

    public AgentTaskResponse getTaskResponse(Long taskId, Long userId) {
        return toResponse(requireOwnedTask(taskId, userId));
    }

    @Transactional
    public void saveFinalReport(Long taskId, FinalReportOutput report) {
        AgentTaskEntity entity = requireById(taskId);
        entity.setReportJson(writeReport(report));
        entity.setUpdateTime(LocalDateTime.now());
        agentTaskMapper.updateById(entity);
    }

    public FinalReportOutput getFinalReport(Long taskId, Long userId) {
        AgentTaskEntity entity = requireOwnedTask(taskId, userId);
        if (entity.getReportJson() == null || entity.getReportJson().isBlank()) {
            throw new BizException(ErrorCode.MATCH_REPORT_NOT_FOUND);
        }
        try {
            return objectMapper.readValue(entity.getReportJson(), FinalReportOutput.class);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), exception.getMessage());
        }
    }

    public AgentTaskResponse toResponse(AgentTaskEntity entity) {
        return AgentTaskResponse.builder()
                .taskId(entity.getId())
                .resumeId(entity.getResumeId())
                .jobDescriptionId(entity.getJobDescriptionId())
                .taskType(entity.getTaskType())
                .status(entity.getStatus())
                .currentNode(entity.getCurrentNode())
                .errorMessage(entity.getErrorMessage())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .createTime(entity.getCreateTime())
                .build();
    }

    private AgentTaskEntity requireById(Long taskId) {
        AgentTaskEntity entity = agentTaskMapper.selectById(taskId);
        if (entity == null) {
            throw new BizException(ErrorCode.AGENT_TASK_NOT_FOUND);
        }
        return entity;
    }

    private String writeReport(FinalReportOutput report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), exception.getMessage());
        }
    }
}
