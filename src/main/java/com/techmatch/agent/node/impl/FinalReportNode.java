package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.FinalReportOutput;
import com.techmatch.agent.output.RejectedClaim;
import com.techmatch.agent.output.VerifiedClaim;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(110)
public class FinalReportNode implements AgentNode {

    @Override
    public String name() {
        return "FinalReportNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getMatchAnalysisOutput() != null
                && context.getHybridScoreResult() != null
                && context.getEvidenceVerificationOutput() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        List<String> strengths = buildVerifiedStrengths(context);
        List<String> risks = buildRisks(context);

        FinalReportOutput report = FinalReportOutput.builder()
                .taskId(context.getTaskId())
                .resumeId(context.getResumeId())
                .jobDescriptionId(context.getJobDescriptionId())
                .candidateName(resolveCandidateName(context))
                .jobTitle(context.getJobTitle())
                .totalScore(context.getHybridScoreResult().getTotalScore())
                .confidence(context.getHybridScoreResult().getConfidence())
                .matchLevel(context.getMatchAnalysisOutput().getMatchLevel())
                .recommendation(context.getMatchAnalysisOutput().getRecommendation())
                .baseConfidence(context.getHybridScoreResult().getConfidence())
                .baseMatchLevel(context.getMatchAnalysisOutput().getMatchLevel())
                .baseRecommendation(context.getMatchAnalysisOutput().getRecommendation())
                .summary(context.getMatchAnalysisOutput().getSummary())
                .dimensionScores(context.getHybridScoreResult().getDimensionScores())
                .baseDimensionScores(context.getHybridScoreResult().getDimensionScores())
                .strengths(strengths)
                .risks(risks)
                .suggestions(context.getMatchAnalysisOutput().getSuggestions())
                .interviewQuestions(context.getInterviewQuestions())
                .build();
        context.setFinalReport(report);

        return AgentNodeResult.success(
                name(),
                "analysis + evidence + questions",
                "最终报告已生成",
                Map.of("matchLevel", report.getMatchLevel(), "riskCount", risks.size())
        );
    }

    private List<String> buildVerifiedStrengths(MatchAgentContext context) {
        List<String> strengths = new ArrayList<>();
        context.getEvidenceVerificationOutput().getVerifiedClaims().stream()
                .filter(item -> "SKILL".equals(item.getClaimType()) || "STRENGTH".equals(item.getClaimType()))
                .filter(item -> "VERIFIED".equals(item.getStatus()) || "PARTIAL".equals(item.getStatus()))
                .map(VerifiedClaim::getClaim)
                .distinct()
                .forEach(strengths::add);

        if (strengths.isEmpty()) {
            strengths.add("当前没有足够强的已核验证据优势，建议结合面试补充判断");
        }
        return strengths;
    }

    private List<String> buildRisks(MatchAgentContext context) {
        List<String> risks = new ArrayList<>(context.getMatchAnalysisOutput().getRisks());
        context.getEvidenceVerificationOutput().getRisks().stream()
                .map(item -> item.getDetail())
                .forEach(risks::add);
        context.getEvidenceVerificationOutput().getRejectedClaims().stream()
                .map(RejectedClaim::getReason)
                .forEach(risks::add);
        return risks.stream().distinct().toList();
    }

    private String resolveCandidateName(MatchAgentContext context) {
        if (context.getResumeData() != null
                && context.getResumeData().getBasics() != null
                && context.getResumeData().getBasics().getName() != null) {
            return context.getResumeData().getBasics().getName();
        }
        return context.getResumeFileName();
    }
}
