package com.techmatch.agent.service.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.output.EvidenceVerificationOutput;
import com.techmatch.agent.output.RejectedClaim;
import com.techmatch.agent.output.RiskOutput;
import com.techmatch.agent.output.VerifiedClaim;
import com.techmatch.agent.service.EvidenceVerificationService;
import com.techmatch.scoring.support.ScoringSupport;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvidenceVerificationServiceImpl implements EvidenceVerificationService {

    @Override
    public EvidenceVerificationOutput verify(MatchAgentContext context) {
        List<VerifiedClaim> verifiedClaims = new ArrayList<>();
        List<RejectedClaim> rejectedClaims = new ArrayList<>();
        Map<String, RiskOutput> riskMap = new LinkedHashMap<>();

        String resumeCorpus = ScoringSupport.joinResumeCorpus(context.getResumeData(), context.getRetrievedChunks());

        context.getHybridScoreResult().getMatchedRequirements().forEach(item -> {
            boolean requirementInResume = containsSupport(item.getRequirement(), resumeCorpus);
            boolean evidenceInResume = containsSupport(item.getEvidence(), resumeCorpus);

            if (evidenceInResume && requirementInResume) {
                verifiedClaims.add(VerifiedClaim.builder()
                        .claimType("SKILL")
                        .requirement(item.getRequirement())
                        .claim("候选人具备 %s".formatted(item.getRequirement()))
                        .evidence(item.getEvidence())
                        .status(item.getSimilarity() != null && item.getSimilarity() >= 0.75 ? "VERIFIED" : "PARTIAL")
                        .build());
                if (item.getSimilarity() == null || item.getSimilarity() < 0.75) {
                    putRisk(riskMap, "partial:" + item.getRequirement(), RiskOutput.builder()
                            .title("部分证据")
                            .requirement(item.getRequirement())
                            .detail("岗位要求“%s”只有部分证据支撑，建议面试继续核验".formatted(item.getRequirement()))
                            .severity("MEDIUM")
                            .build());
                }
                return;
            }

            if (evidenceInResume) {
                verifiedClaims.add(VerifiedClaim.builder()
                        .claimType("SKILL")
                        .requirement(item.getRequirement())
                        .claim("候选人可能具备 %s".formatted(item.getRequirement()))
                        .evidence(item.getEvidence())
                        .status("PARTIAL")
                        .build());
                putRisk(riskMap, "partial:" + item.getRequirement(), RiskOutput.builder()
                        .title("部分证据")
                        .requirement(item.getRequirement())
                        .detail("检索片段与岗位要求“%s”相关，但简历技能声明不足".formatted(item.getRequirement()))
                        .severity("MEDIUM")
                        .build());
                return;
            }

            rejectedClaims.add(RejectedClaim.builder()
                    .claimType("SKILL")
                    .requirement(item.getRequirement())
                    .claim("候选人具备 %s".formatted(item.getRequirement()))
                    .reason("未找到绑定到简历的有效证据，可能是将 JD 要求误判为候选人能力")
                    .build());
            putRisk(riskMap, "missing:" + item.getRequirement(), RiskOutput.builder()
                    .title("缺失证据")
                    .requirement(item.getRequirement())
                    .detail("岗位要求“%s”缺少可靠简历证据，需转为面试验证问题".formatted(item.getRequirement()))
                    .severity("HIGH")
                    .build());
        });

        context.getHybridScoreResult().getMissingRequirements().forEach(item -> {
            rejectedClaims.add(RejectedClaim.builder()
                    .claimType("SKILL")
                    .requirement(item.getRequirement())
                    .claim("候选人可能缺少 %s".formatted(item.getRequirement()))
                    .reason(item.getReason())
                    .build());
            putRisk(riskMap, "missing:" + item.getRequirement(), RiskOutput.builder()
                    .title("缺失证据")
                    .requirement(item.getRequirement())
                    .detail("岗位要求“%s”缺失证据：%s".formatted(item.getRequirement(), item.getReason()))
                    .severity("HIGH")
                    .build());
        });

        if (context.getMatchAnalysisOutput() != null && context.getMatchAnalysisOutput().getStrengths() != null) {
            context.getMatchAnalysisOutput().getStrengths().forEach(strength -> {
                if (containsSupport(strength, resumeCorpus)) {
                    verifiedClaims.add(VerifiedClaim.builder()
                            .claimType("STRENGTH")
                            .requirement(null)
                            .claim(strength)
                            .evidence(findEvidenceFragment(strength, context))
                            .status("PARTIAL")
                            .build());
                } else {
                    rejectedClaims.add(RejectedClaim.builder()
                            .claimType("STRENGTH")
                            .requirement(null)
                            .claim(strength)
                            .reason("优势结论未绑定到明确简历证据")
                            .build());
                }
            });
        }

        context.getWarnings().forEach(item ->
                putRisk(riskMap, "warning:" + item, RiskOutput.builder()
                        .title("输入质量风险")
                        .requirement(null)
                        .detail(item)
                        .severity("MEDIUM")
                        .build())
        );

        return EvidenceVerificationOutput.builder()
                .verifiedClaims(verifiedClaims)
                .rejectedClaims(rejectedClaims)
                .risks(new ArrayList<>(riskMap.values()))
                .build();
    }

    private boolean containsSupport(String text, String resumeCorpus) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(resumeCorpus)) {
            return false;
        }
        List<String> keywords = ScoringSupport.extractKeywords(text);
        if (keywords.isEmpty()) {
            return resumeCorpus.contains(ScoringSupport.normalizeText(text));
        }
        long matched = keywords.stream().filter(resumeCorpus::contains).count();
        return matched >= Math.max(1, Math.min(2, keywords.size()));
    }

    private String findEvidenceFragment(String claim, MatchAgentContext context) {
        return context.getRetrievedChunks().stream()
                .map(item -> item.getChunkText())
                .filter(text -> containsSupport(claim, ScoringSupport.normalizeText(text)))
                .findFirst()
                .orElse("简历语料存在相关片段，但未定位到单一证据块");
    }

    private void putRisk(Map<String, RiskOutput> riskMap, String key, RiskOutput value) {
        riskMap.putIfAbsent(key, value);
    }
}
