package com.techmatch.task.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AgentTimelineEventResponse {
    Long eventId;
    String nodeName;
    String status;
    String inputSummary;
    String outputSummary;
    String modelName;
    Integer promptTokens;
    Integer completionTokens;
    Long durationMs;
    Integer retryCount;
    String errorMessage;
    String metadataJson;
    LocalDateTime createTime;
}
