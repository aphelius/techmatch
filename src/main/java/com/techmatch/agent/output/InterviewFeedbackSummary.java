package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class InterviewFeedbackSummary {
    Integer feedbackCount;
    BigDecimal averageScore;
    BigDecimal confidenceDelta;
    String adjustedMatchLevel;
    String adjustedRecommendation;
    String note;
}
