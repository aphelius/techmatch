package com.techmatch.scoring.rule;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.MatchedRequirement;
import com.techmatch.agent.output.MissingRequirement;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.scoring.support.ScoringSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SkillMatchRule {

    public SkillMatchResult evaluate(Set<String> resumeSkills,
                                     Set<String> requiredSkills,
                                     List<RetrievedChunk> retrievedChunks) {
        if (requiredSkills.isEmpty()) {
            return SkillMatchResult.builder()
                    .dimensionScore(zeroDimension())
                    .matchedRequirements(List.of())
                    .missingRequirements(List.of())
                    .evidenceCoverage(0D)
                    .build();
        }

        List<MatchedRequirement> matched = new ArrayList<>();
        List<MissingRequirement> missing = new ArrayList<>();
        double weightedHits = 0D;
        int evidencedCount = 0;

        for (String requiredSkill : requiredSkills) {
            RetrievedChunk evidenceChunk = findEvidenceChunk(requiredSkill, retrievedChunks);
            boolean declared = resumeSkills.contains(requiredSkill);
            boolean evidenced = evidenceChunk != null;

            if (declared && evidenced) {
                evidencedCount++;
                weightedHits += 1D;
                matched.add(MatchedRequirement.builder()
                        .requirement(requiredSkill)
                        .evidence(evidenceChunk.getChunkText())
                        .similarity(evidenceChunk.getSimilarity())
                        .build());
                continue;
            }

            if (declared) {
                weightedHits += 0.45D;
                missing.add(MissingRequirement.builder()
                        .requirement(requiredSkill)
                        .reason("技能列表命中，但缺少检索证据支撑")
                        .suggestion("建议在简历项目经历中补充该技能的落地场景")
                        .build());
                continue;
            }

            if (evidenced) {
                evidencedCount++;
                weightedHits += 0.65D;
                matched.add(MatchedRequirement.builder()
                        .requirement(requiredSkill)
                        .evidence(evidenceChunk.getChunkText())
                        .similarity(evidenceChunk.getSimilarity())
                        .build());
                missing.add(MissingRequirement.builder()
                        .requirement(requiredSkill)
                        .reason("检索到局部证据，但技能标签未明确声明")
                        .suggestion("建议在简历技能或项目总结中明确标注该能力")
                        .build());
                continue;
            }

            missing.add(MissingRequirement.builder()
                    .requirement(requiredSkill)
                    .reason("技能标签与检索证据中都未发现命中")
                    .suggestion("建议在面试中重点核验该项能力")
                    .build());
        }

        BigDecimal score = BigDecimal.valueOf(weightedHits)
                .multiply(BigDecimal.valueOf(35))
                .divide(BigDecimal.valueOf(requiredSkills.size()), 2, RoundingMode.HALF_UP);

        double coverage = (double) evidencedCount / requiredSkills.size();
        DimensionScore dimension = DimensionScore.builder()
                .dimension("TECH_SKILL")
                .score(score)
                .maxScore(BigDecimal.valueOf(35))
                .confidence(BigDecimal.valueOf(Math.max(0.45, Math.min(0.95, 0.55 + coverage * 0.35))))
                .reason("必备技能命中 %d/%d，其中有证据支撑 %d 项".formatted(
                        matched.size(),
                        requiredSkills.size(),
                        evidencedCount
                ))
                .build();

        return SkillMatchResult.builder()
                .dimensionScore(dimension)
                .matchedRequirements(matched)
                .missingRequirements(missing)
                .evidenceCoverage(coverage)
                .build();
    }

    private RetrievedChunk findEvidenceChunk(String requiredSkill, List<RetrievedChunk> retrievedChunks) {
        String normalizedSkill = ScoringSupport.normalizeText(requiredSkill);
        return retrievedChunks.stream()
                .filter(item -> ScoringSupport.normalizeText(item.getChunkText()).contains(normalizedSkill))
                .findFirst()
                .orElse(null);
    }

    private DimensionScore zeroDimension() {
        return DimensionScore.builder()
                .dimension("TECH_SKILL")
                .score(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .maxScore(BigDecimal.valueOf(35))
                .confidence(BigDecimal.valueOf(0.6))
                .reason("岗位未配置必备技能")
                .build();
    }
}
