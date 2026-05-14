package com.techmatch.resume.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeChunkSearchResponse {

    private Long chunkId;
    private Long resumeId;
    private String fileName;
    private Integer chunkIndex;
    private String chunkText;
    private String metadataJson;
    private Double distance;
    private Double similarity;
}
