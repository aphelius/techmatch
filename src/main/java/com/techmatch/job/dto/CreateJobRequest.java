package com.techmatch.job.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateJobRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String company;

    private String location;

    @NotBlank(message = "rawText is required")
    private String rawText;
}
