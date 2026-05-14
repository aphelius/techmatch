package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class HybridScoreResult {
    BigDecimal totalScore;
    BigDecimal confidence;
    List<DimensionScore> dimensionScores;
    List<MatchedRequirement> matchedRequirements;
    List<MissingRequirement> missingRequirements;
}
