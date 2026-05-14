package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RejectedClaim {
    String claimType;
    String requirement;
    String claim;
    String reason;
}
