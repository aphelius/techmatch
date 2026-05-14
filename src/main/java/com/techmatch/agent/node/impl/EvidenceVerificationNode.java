package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.EvidenceVerificationOutput;
import com.techmatch.agent.service.EvidenceVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(80)
@RequiredArgsConstructor
public class EvidenceVerificationNode implements AgentNode {

    private final EvidenceVerificationService evidenceVerificationService;

    @Override
    public String name() {
        return "EvidenceVerificationNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getHybridScoreResult() != null && context.getMatchAnalysisOutput() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        EvidenceVerificationOutput output = evidenceVerificationService.verify(context);
        context.setEvidenceVerificationOutput(output);

        return AgentNodeResult.success(
                name(),
                "matchAnalysis + hybridScoreResult",
                "证据核验完成，确认 %d 条，拒绝 %d 条".formatted(
                        output.getVerifiedClaims().size(),
                        output.getRejectedClaims().size()
                ),
                Map.of(
                        "verified", output.getVerifiedClaims().size(),
                        "rejected", output.getRejectedClaims().size(),
                        "risks", output.getRisks().size()
                )
        );
    }
}
