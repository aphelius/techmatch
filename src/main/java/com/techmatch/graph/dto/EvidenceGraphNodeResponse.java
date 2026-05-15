package com.techmatch.graph.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EvidenceGraphNodeResponse {
    Long nodeId;
    String nodeType;
    String nodeKey;
    String title;
    String content;
    String status;
    String metadataJson;
}
