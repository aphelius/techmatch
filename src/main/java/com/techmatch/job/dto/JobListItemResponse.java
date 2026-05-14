package com.techmatch.job.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobListItemResponse {

    private Long jobId;
    private String title;
    private String company;
    private String location;
    private String status;
    private LocalDateTime createTime;
}
