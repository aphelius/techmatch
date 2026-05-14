package com.techmatch.agent.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.FinalReportOutput;
import com.techmatch.task.service.AgentTaskService;
import com.techmatch.task.service.AgentTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchAgentOrchestrator {

    private static final int MAX_RETRY = 2;

    private final List<AgentNode> nodes;
    private final AgentTaskService agentTaskService;
    private final AgentTimelineService timelineService;
    private final ObjectMapper objectMapper;

    public FinalReportOutput run(MatchAgentContext context) {
        AgentNode lastNode = null;
        try {
            for (AgentNode node : nodes) {
                if (!node.shouldExecute(context)) {
                    continue;
                }
                lastNode = node;
                agentTaskService.markRunning(context.getTaskId(), node.name());

                AgentNodeResult result = executeWithRetry(node, context);
                if (!result.isSuccess()) {
                    String message = result.getMessage() == null ? "Agent node failed" : result.getMessage();
                    agentTaskService.markFailed(context.getTaskId(), node.name(), message);
                    throw new IllegalStateException(message);
                }
            }

            agentTaskService.markCompleted(context.getTaskId(), lastNode == null ? "COMPLETED" : lastNode.name());
            return context.getFinalReport();
        } catch (RuntimeException exception) {
            log.warn("Match agent task failed: taskId={}, node={}, message={}",
                    context.getTaskId(),
                    lastNode == null ? "N/A" : lastNode.name(),
                    exception.getMessage());
            throw exception;
        }
    }

    private AgentNodeResult executeWithRetry(AgentNode node, MatchAgentContext context) {
        int retryCount = 0;

        while (true) {
            long start = System.currentTimeMillis();
            try {
                AgentNodeResult result = node.execute(context);
                long durationMs = System.currentTimeMillis() - start;

                if (!result.isSuccess()) {
                    timelineService.recordFailed(
                            context.getTaskId(),
                            node.name(),
                            result.getInputSummary(),
                            result.getOutputSummary(),
                            result.getModelName(),
                            result.getPromptTokens(),
                            result.getCompletionTokens(),
                            durationMs,
                            retryCount,
                            result.getMessage(),
                            writeMetadata(result.getMetadata())
                    );
                    if (result.isRetryable() && retryCount < MAX_RETRY) {
                        retryCount++;
                        continue;
                    }
                    return AgentNodeResult.failed(node.name(), result.getMessage(), false);
                }

                timelineService.recordSuccess(
                        context.getTaskId(),
                        node.name(),
                        result.getInputSummary(),
                        result.getOutputSummary(),
                        result.getModelName(),
                        result.getPromptTokens(),
                        result.getCompletionTokens(),
                        durationMs,
                        retryCount,
                        writeMetadata(result.getMetadata())
                );
                return result;
            } catch (Exception exception) {
                long durationMs = System.currentTimeMillis() - start;
                timelineService.recordFailed(
                        context.getTaskId(),
                        node.name(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        durationMs,
                        retryCount,
                        exception.getMessage(),
                        null
                );
                if (retryCount >= MAX_RETRY) {
                    return AgentNodeResult.failed(node.name(), exception.getMessage(), false);
                }
                retryCount++;
            }
        }
    }

    private String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"%s\"}".formatted(exception.getMessage());
        }
    }
}
