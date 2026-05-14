package com.techmatch.job.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobCreateResponse {

    private Long jobId;
    private String title;
    private String status;
    private Integer chunkCount;
}
