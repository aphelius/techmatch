package com.techmatch.match.dto;

import com.techmatch.graph.dto.EvidenceGraphChainResponse;
import com.techmatch.graph.dto.EvidenceGraphEdgeResponse;
import com.techmatch.graph.dto.EvidenceGraphNodeResponse;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MatchEvidenceGraphResponse {
    Long taskId;
    List<EvidenceGraphNodeResponse> nodes;
    List<EvidenceGraphEdgeResponse> edges;
    List<EvidenceGraphChainResponse> chains;
}
