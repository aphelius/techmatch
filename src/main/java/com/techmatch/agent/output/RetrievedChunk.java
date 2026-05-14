package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RetrievedChunk {
    Long chunkId;
    Integer chunkIndex;
    String chunkText;
    Double similarity;
    String reason;
}
