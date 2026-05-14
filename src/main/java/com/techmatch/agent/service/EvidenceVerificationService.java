package com.techmatch.agent.service;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.output.EvidenceVerificationOutput;

public interface EvidenceVerificationService {

    EvidenceVerificationOutput verify(MatchAgentContext context);
}
