package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.graph.service.EvidenceGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(105)
@RequiredArgsConstructor
public class EvidenceGraphBuildNode implements AgentNode {

    private final EvidenceGraphService evidenceGraphService;

    @Override
    public String name() {
        return "EvidenceGraphBuildNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getEvidenceVerificationOutput() != null && context.getInterviewQuestions() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        evidenceGraphService.rebuild(context);

        int nodeCount = context.getEvidenceVerificationOutput().getVerifiedClaims().size()
                + context.getEvidenceVerificationOutput().getRejectedClaims().size()
                + context.getEvidenceVerificationOutput().getRisks().size()
                + context.getInterviewQuestions().size();
        int edgeCount = context.getEvidenceVerificationOutput().getVerifiedClaims().size()
                + context.getEvidenceVerificationOutput().getRisks().size()
                + context.getInterviewQuestions().size();

        context.getAttributes().put("evidenceGraph", Map.of("nodeCount", nodeCount, "edgeCount", edgeCount));

        return AgentNodeResult.success(
                name(),
                "verificationOutput + interviewQuestions",
                "证据图谱已构建并落库",
                Map.of("nodeCount", nodeCount, "edgeCount", edgeCount)
        );
    }
}
