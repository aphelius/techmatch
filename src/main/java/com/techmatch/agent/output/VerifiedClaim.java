package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerifiedClaim {
    String claimType;
    String requirement;
    String claim;
    String evidence;
    String status;
}
