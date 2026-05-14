package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class DimensionScore {
    String dimension;
    BigDecimal score;
    BigDecimal maxScore;
    BigDecimal confidence;
    String reason;
}
