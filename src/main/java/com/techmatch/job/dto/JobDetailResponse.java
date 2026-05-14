package com.techmatch.job.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JobDetailResponse {

    private Long jobId;
    private String title;
    private String company;
    private String location;
    private String status;
    private String rawText;
    private JobStructuredData structured;
    private String parseError;
    private LocalDateTime createTime;
    private List<JobChunkResponse> chunks;
}
