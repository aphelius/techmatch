package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MissingRequirement {
    String requirement;
    String reason;
    String suggestion;
}
