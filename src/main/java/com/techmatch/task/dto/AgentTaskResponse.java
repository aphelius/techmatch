package com.techmatch.task.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AgentTaskResponse {
    Long taskId;
    Long resumeId;
    Long jobDescriptionId;
    String taskType;
    String status;
    String currentNode;
    String errorMessage;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    LocalDateTime createTime;
}
