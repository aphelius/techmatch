package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RiskOutput {
    String title;
    String requirement;
    String detail;
    String severity;
}
