package com.techmatch.match.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class InterviewFeedbackDetailResponse {
    Long feedbackId;
    Long questionId;
    Integer score;
    String feedbackType;
    String notes;
    BigDecimal confidenceDelta;
    LocalDateTime createTime;
    LocalDateTime updateTime;
}
