package com.techmatch.agent.node;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class AgentNodeResult {
    boolean success;
    String nodeName;
    String message;
    boolean shouldStop;
    boolean retryable;
    String inputSummary;
    String outputSummary;
    String modelName;
    Integer promptTokens;
    Integer completionTokens;
    Map<String, Object> metadata;

    public static AgentNodeResult success(String nodeName, String outputSummary) {
        return AgentNodeResult.builder()
                .success(true)
                .nodeName(nodeName)
                .outputSummary(outputSummary)
                .shouldStop(false)
                .retryable(false)
                .build();
    }

    public static AgentNodeResult success(String nodeName,
                                          String inputSummary,
                                          String outputSummary,
                                          Map<String, Object> metadata) {
        return AgentNodeResult.builder()
                .success(true)
                .nodeName(nodeName)
                .inputSummary(inputSummary)
                .outputSummary(outputSummary)
                .metadata(metadata)
                .shouldStop(false)
                .retryable(false)
                .build();
    }

    public static AgentNodeResult failed(String nodeName, String message, boolean retryable) {
        return AgentNodeResult.builder()
                .success(false)
                .nodeName(nodeName)
                .message(message)
                .retryable(retryable)
                .shouldStop(!retryable)
                .build();
    }
}
