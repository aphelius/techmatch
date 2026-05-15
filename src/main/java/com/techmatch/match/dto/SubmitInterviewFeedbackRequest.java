package com.techmatch.match.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitInterviewFeedbackRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    @NotNull(message = "score is required")
    @Min(value = 1, message = "score must be between 1 and 5")
    @Max(value = 5, message = "score must be between 1 and 5")
    private Integer score;

    @NotBlank(message = "feedbackType is required")
    private String feedbackType;

    private String notes;
}
