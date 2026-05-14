package com.techmatch.resume.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ResumeDetailResponse {

    private Long resumeId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private String storageUrl;
    private String rawText;
    private StructuredResumeDto structured;
    private String parseError;
    private LocalDateTime createTime;
    private List<ResumeChunkResponse> chunks;
}
