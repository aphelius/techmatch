package com.techmatch.graph.service;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.graph.dto.EvidenceGraphResponse;

public interface EvidenceGraphService {

    void rebuild(MatchAgentContext context);

    EvidenceGraphResponse getGraph(Long taskId, Long userId);
}
