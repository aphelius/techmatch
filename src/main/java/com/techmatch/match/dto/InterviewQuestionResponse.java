package com.techmatch.match.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterviewQuestionResponse {
    Long questionId;
    String type;
    String question;
    String target;
    String difficulty;
    String sourceRisk;
    InterviewFeedbackDetailResponse feedback;
}
