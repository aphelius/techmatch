package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MatchedRequirement {
    String requirement;
    String evidence;
    Double similarity;
}
