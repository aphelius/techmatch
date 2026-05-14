package com.techmatch.scoring.scorer;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.resume.dto.StructuredResumeDto;
import com.techmatch.scoring.support.ScoringSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Component
public class BusinessKeywordScorer {

    public DimensionScore score(StructuredResumeDto resumeData,
                                Set<String> businessKeywords,
                                List<RetrievedChunk> retrievedChunks) {
        if (businessKeywords.isEmpty()) {
            return zeroDimension("岗位未提炼业务关键词");
        }

        String resumeCorpus = ScoringSupport.joinResumeCorpus(resumeData, retrievedChunks);
        int hitCount = 0;
        for (String keyword : businessKeywords) {
            if (resumeCorpus.contains(keyword)) {
                hitCount++;
            }
        }

        BigDecimal score = BigDecimal.valueOf(hitCount)
                .multiply(BigDecimal.valueOf(15))
                .divide(BigDecimal.valueOf(businessKeywords.size()), 2, RoundingMode.HALF_UP);

        double ratio = (double) hitCount / businessKeywords.size();
        return DimensionScore.builder()
                .dimension("BUSINESS_DOMAIN")
                .score(score)
                .maxScore(BigDecimal.valueOf(15))
                .confidence(BigDecimal.valueOf(Math.max(0.45, Math.min(0.9, 0.55 + ratio * 0.3))))
                .reason("业务关键词命中 %d/%d".formatted(hitCount, businessKeywords.size()))
                .build();
    }

    private DimensionScore zeroDimension(String reason) {
        return DimensionScore.builder()
                .dimension("BUSINESS_DOMAIN")
                .score(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .maxScore(BigDecimal.valueOf(15))
                .confidence(BigDecimal.valueOf(0.6))
                .reason(reason)
                .build();
    }
}
