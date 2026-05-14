package com.techmatch.resume.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeListItemResponse {

    private Long resumeId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private LocalDateTime createTime;
}
