package com.techmatch.match.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMatchRequest {

    @NotNull(message = "resumeId is required")
    private Long resumeId;

    @NotNull(message = "jobDescriptionId is required")
    private Long jobDescriptionId;
}
