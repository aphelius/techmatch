package com.techmatch.scoring.scorer;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.resume.dto.StructuredResumeDto;
import com.techmatch.scoring.support.ScoringSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ProjectSimilarityScorer {

    public DimensionScore score(StructuredResumeDto resumeData,
                                JobStructuredData jobData,
                                List<RetrievedChunk> retrievedChunks) {
        List<String> signals = ScoringSupport.collectJobSignals(jobData);
        if (signals.isEmpty()) {
            return zeroDimension("岗位职责与要求为空，暂不给项目经历分");
        }

        String projectCorpus = ScoringSupport.joinProjectCorpus(resumeData, retrievedChunks);
        double totalSimilarity = 0D;
        int strongHits = 0;

        for (String signal : signals) {
            double similarity = Math.max(
                    ScoringSupport.overlapScore(projectCorpus, signal),
                    bestChunkSimilarity(signal, retrievedChunks)
            );
            if (similarity >= 0.5) {
                strongHits++;
            }
            totalSimilarity += similarity;
        }

        double averageSimilarity = totalSimilarity / signals.size();
        BigDecimal score = BigDecimal.valueOf(averageSimilarity)
                .multiply(BigDecimal.valueOf(30))
                .setScale(2, RoundingMode.HALF_UP);

        return DimensionScore.builder()
                .dimension("PROJECT_EXPERIENCE")
                .score(score)
                .maxScore(BigDecimal.valueOf(30))
                .confidence(BigDecimal.valueOf(Math.max(0.45, Math.min(0.9, 0.55 + averageSimilarity * 0.35))))
                .reason("岗位职责/要求强相关命中 %d/%d，平均相似度 %.2f".formatted(strongHits, signals.size(), averageSimilarity))
                .build();
    }

    private double bestChunkSimilarity(String signal, List<RetrievedChunk> retrievedChunks) {
        return retrievedChunks.stream()
                .mapToDouble(chunk -> ScoringSupport.overlapScore(signal, chunk.getChunkText()))
                .max()
                .orElse(0D);
    }

    private DimensionScore zeroDimension(String reason) {
        return DimensionScore.builder()
                .dimension("PROJECT_EXPERIENCE")
                .score(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .maxScore(BigDecimal.valueOf(30))
                .confidence(BigDecimal.valueOf(0.6))
                .reason(reason)
                .build();
    }
}
