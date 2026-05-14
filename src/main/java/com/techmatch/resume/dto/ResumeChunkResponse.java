package com.techmatch.resume.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResumeChunkResponse {

    private Long chunkId;
    private Integer chunkIndex;
    private String chunkText;
    private String metadataJson;
    private List<Double> embedding;
    private Integer embeddingDimensions;
}
