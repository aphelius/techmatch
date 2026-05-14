package com.techmatch.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techmatch.task.dto.AgentTimelineEventResponse;
import com.techmatch.task.entity.AgentTaskEntity;
import com.techmatch.task.entity.AgentTimelineEventEntity;
import com.techmatch.task.mapper.AgentTimelineEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentTimelineService {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final AgentTaskService agentTaskService;
    private final AgentTimelineEventMapper agentTimelineEventMapper;

    @Transactional
    public void recordCreated(Long taskId, String outputSummary) {
        record(taskId, "MatchTaskCreate", STATUS_CREATED, null, outputSummary,
                null, null, null, null, 0, null, null);
    }

    @Transactional
    public void recordSuccess(Long taskId,
                              String nodeName,
                              String inputSummary,
                              String outputSummary,
                              String modelName,
                              Integer promptTokens,
                              Integer completionTokens,
                              Long durationMs,
                              Integer retryCount,
                              String metadataJson) {
        record(taskId, nodeName, STATUS_SUCCESS, inputSummary, outputSummary, modelName,
                promptTokens, completionTokens, durationMs, retryCount, null, metadataJson);
    }

    @Transactional
    public void recordFailed(Long taskId,
                             String nodeName,
                             String inputSummary,
                             String outputSummary,
                             String modelName,
                             Integer promptTokens,
                             Integer completionTokens,
                             Long durationMs,
                             Integer retryCount,
                             String errorMessage,
                             String metadataJson) {
        record(taskId, nodeName, STATUS_FAILED, inputSummary, outputSummary, modelName,
                promptTokens, completionTokens, durationMs, retryCount, errorMessage, metadataJson);
    }

    public List<AgentTimelineEventResponse> listTimeline(Long taskId, Long userId) {
        AgentTaskEntity task = agentTaskService.requireOwnedTask(taskId, userId);
        return agentTimelineEventMapper.selectList(new LambdaQueryWrapper<AgentTimelineEventEntity>()
                        .eq(AgentTimelineEventEntity::getTaskId, task.getId())
                        .orderByAsc(AgentTimelineEventEntity::getCreateTime)
                        .orderByAsc(AgentTimelineEventEntity::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void record(Long taskId,
                        String nodeName,
                        String status,
                        String inputSummary,
                        String outputSummary,
                        String modelName,
                        Integer promptTokens,
                        Integer completionTokens,
                        Long durationMs,
                        Integer retryCount,
                        String errorMessage,
                        String metadataJson) {
        AgentTimelineEventEntity entity = new AgentTimelineEventEntity();
        entity.setTaskId(taskId);
        entity.setNodeName(nodeName);
        entity.setStatus(status);
        entity.setInputSummary(inputSummary);
        entity.setOutputSummary(outputSummary);
        entity.setModelName(modelName);
        entity.setPromptTokens(promptTokens);
        entity.setCompletionTokens(completionTokens);
        entity.setDurationMs(durationMs);
        entity.setRetryCount(retryCount == null ? 0 : retryCount);
        entity.setErrorMessage(errorMessage);
        entity.setMetadataJson(metadataJson);
        entity.setCreateTime(LocalDateTime.now());
        agentTimelineEventMapper.insert(entity);
    }

    private AgentTimelineEventResponse toResponse(AgentTimelineEventEntity entity) {
        return AgentTimelineEventResponse.builder()
                .eventId(entity.getId())
                .nodeName(entity.getNodeName())
                .status(entity.getStatus())
                .inputSummary(entity.getInputSummary())
                .outputSummary(entity.getOutputSummary())
                .modelName(entity.getModelName())
                .promptTokens(entity.getPromptTokens())
                .completionTokens(entity.getCompletionTokens())
                .durationMs(entity.getDurationMs())
                .retryCount(entity.getRetryCount())
                .errorMessage(entity.getErrorMessage())
                .metadataJson(entity.getMetadataJson())
                .createTime(entity.getCreateTime())
                .build();
    }
}
