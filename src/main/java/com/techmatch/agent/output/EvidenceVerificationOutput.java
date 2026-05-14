package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EvidenceVerificationOutput {
    List<VerifiedClaim> verifiedClaims;
    List<RejectedClaim> rejectedClaims;
    List<RiskOutput> risks;
}
