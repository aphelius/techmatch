package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class FinalReportOutput {
    Long taskId;
    Long resumeId;
    Long jobDescriptionId;
    String candidateName;
    String jobTitle;
    BigDecimal totalScore;
    BigDecimal confidence;
    String matchLevel;
    String recommendation;
    String summary;
    List<DimensionScore> dimensionScores;
    List<String> strengths;
    List<String> risks;
    List<String> suggestions;
    List<InterviewQuestionOutput> interviewQuestions;
}
