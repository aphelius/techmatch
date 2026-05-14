package com.techmatch.scoring.rule;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.MatchedRequirement;
import com.techmatch.agent.output.MissingRequirement;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SkillMatchResult {
    DimensionScore dimensionScore;
    List<MatchedRequirement> matchedRequirements;
    List<MissingRequirement> missingRequirements;
    double evidenceCoverage;
}
