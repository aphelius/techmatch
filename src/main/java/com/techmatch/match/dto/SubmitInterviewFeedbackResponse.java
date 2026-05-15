package com.techmatch.match.dto;

import com.techmatch.agent.output.InterviewFeedbackSummary;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class SubmitInterviewFeedbackResponse {
    Long taskId;
    Long questionId;
    Long feedbackId;
    BigDecimal confidence;
    String matchLevel;
    String recommendation;
    InterviewFeedbackSummary feedbackSummary;
}
