package com.techmatch.scoring.scorer;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.RetrievedChunk;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ConfidenceCalculator {

    public BigDecimal calculate(List<DimensionScore> dimensions,
                                List<RetrievedChunk> retrievedChunks,
                                List<String> warnings,
                                double requiredSkillEvidenceCoverage) {
        double dimensionConfidence = dimensions.stream()
                .map(DimensionScore::getConfidence)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.6D);

        double retrievalConfidence = retrievedChunks.stream()
                .map(RetrievedChunk::getSimilarity)
                .mapToDouble(value -> value == null ? 0D : value)
                .average()
                .orElse(0.35D);

        double warningPenalty = Math.min(0.18, warnings.size() * 0.05);
        double score = dimensionConfidence * 0.45
                + retrievalConfidence * 0.3
                + requiredSkillEvidenceCoverage * 0.25
                - warningPenalty;

        double bounded = Math.max(0.2, Math.min(0.95, score));
        return BigDecimal.valueOf(bounded).setScale(2, RoundingMode.HALF_UP);
    }
}
