package com.techmatch.graph.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EvidenceGraphResponse {
    Long taskId;
    List<EvidenceGraphNodeResponse> nodes;
    List<EvidenceGraphEdgeResponse> edges;
    List<EvidenceGraphChainResponse> chains;
}
