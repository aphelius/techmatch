package com.techmatch.match.dto;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.InterviewQuestionOutput;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class MatchReportResponse {
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
