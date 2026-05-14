package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MatchAnalysisOutput {
    String matchLevel;
    String recommendation;
    String summary;
    List<String> strengths;
    List<String> risks;
    List<String> suggestions;
}
