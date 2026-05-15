package com.techmatch.graph.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EvidenceGraphEdgeResponse {
    Long edgeId;
    Long fromNodeId;
    Long toNodeId;
    String edgeType;
    String metadataJson;
}
