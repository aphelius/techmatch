package com.techmatch.match.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CreateMatchResponse {
    Long taskId;
    Long resumeId;
    Long jobDescriptionId;
    String taskType;
    String status;
    String currentNode;
    LocalDateTime createTime;
}
