package com.techmatch.scoring.engine.impl;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.HybridScoreResult;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.resume.dto.StructuredResumeDto;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.scoring.engine.HybridScoringEngine;
import com.techmatch.scoring.rule.SkillMatchResult;
import com.techmatch.scoring.rule.SkillMatchRule;
import com.techmatch.scoring.scorer.BusinessKeywordScorer;
import com.techmatch.scoring.scorer.ConfidenceCalculator;
import com.techmatch.scoring.scorer.ExperienceEducationScorer;
import com.techmatch.scoring.scorer.PreferredSkillScorer;
import com.techmatch.scoring.scorer.ProjectSimilarityScorer;
import com.techmatch.scoring.support.ScoringSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HybridScoringEngineImpl implements HybridScoringEngine {

    private final SkillMatchRule skillMatchRule;
    private final ProjectSimilarityScorer projectSimilarityScorer;
    private final BusinessKeywordScorer businessKeywordScorer;
    private final ExperienceEducationScorer experienceEducationScorer;
    private final PreferredSkillScorer preferredSkillScorer;
    private final ConfidenceCalculator confidenceCalculator;

    @Override
    public HybridScoreResult score(StructuredResumeDto resumeData,
                                   JobStructuredData jobData,
                                   List<RetrievedChunk> retrievedChunks,
                                   List<String> warnings) {
        Set<String> resumeSkills = ScoringSupport.normalizeSet(resumeData.getSkills());
        Set<String> requiredSkills = ScoringSupport.normalizeSet(jobData.getRequiredSkills());
        Set<String> preferredSkills = ScoringSupport.normalizeSet(jobData.getPreferredSkills());
        Set<String> businessKeywords = ScoringSupport.normalizeSet(jobData.getBusinessKeywords());

        SkillMatchResult skillMatchResult = skillMatchRule.evaluate(resumeSkills, requiredSkills, retrievedChunks);
        DimensionScore projectScore = projectSimilarityScorer.score(resumeData, jobData, retrievedChunks);
        DimensionScore businessScore = businessKeywordScorer.score(resumeData, businessKeywords, retrievedChunks);
        DimensionScore experienceScore = experienceEducationScorer.score(resumeData, jobData);
        DimensionScore preferredScore = preferredSkillScorer.score(resumeSkills, preferredSkills, retrievedChunks);

        List<DimensionScore> dimensions = new ArrayList<>();
        dimensions.add(skillMatchResult.getDimensionScore());
        dimensions.add(projectScore);
        dimensions.add(businessScore);
        dimensions.add(experienceScore);
        dimensions.add(preferredScore);

        BigDecimal totalScore = dimensions.stream()
                .map(DimensionScore::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal confidence = confidenceCalculator.calculate(
                dimensions,
                retrievedChunks,
                warnings,
                skillMatchResult.getEvidenceCoverage()
        );

        return HybridScoreResult.builder()
                .totalScore(totalScore)
                .confidence(confidence)
                .dimensionScores(dimensions)
                .matchedRequirements(skillMatchResult.getMatchedRequirements())
                .missingRequirements(skillMatchResult.getMissingRequirements())
                .build();
    }
}
