package com.techmatch.scoring.scorer;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.scoring.support.ScoringSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Component
public class PreferredSkillScorer {

    public DimensionScore score(Set<String> resumeSkills,
                                Set<String> preferredSkills,
                                List<RetrievedChunk> retrievedChunks) {
        if (preferredSkills.isEmpty()) {
            return zeroDimension("岗位未配置加分技能");
        }

        double weightedHits = 0D;
        int hitCount = 0;
        for (String preferredSkill : preferredSkills) {
            boolean declared = resumeSkills.contains(preferredSkill);
            boolean evidenced = retrievedChunks.stream()
                    .anyMatch(chunk -> ScoringSupport.normalizeText(chunk.getChunkText()).contains(preferredSkill));
            if (declared && evidenced) {
                weightedHits += 1D;
                hitCount++;
            } else if (declared || evidenced) {
                weightedHits += 0.5D;
                hitCount++;
            }
        }

        BigDecimal score = BigDecimal.valueOf(weightedHits)
                .multiply(BigDecimal.valueOf(10))
                .divide(BigDecimal.valueOf(preferredSkills.size()), 2, RoundingMode.HALF_UP);
        double ratio = weightedHits / preferredSkills.size();

        return DimensionScore.builder()
                .dimension("PREFERRED_SKILL")
                .score(score)
                .maxScore(BigDecimal.valueOf(10))
                .confidence(BigDecimal.valueOf(Math.max(0.45, Math.min(0.9, 0.55 + ratio * 0.3))))
                .reason("加分技能命中 %d/%d".formatted(hitCount, preferredSkills.size()))
                .build();
    }

    private DimensionScore zeroDimension(String reason) {
        return DimensionScore.builder()
                .dimension("PREFERRED_SKILL")
                .score(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .maxScore(BigDecimal.valueOf(10))
                .confidence(BigDecimal.valueOf(0.6))
                .reason(reason)
                .build();
    }
}
