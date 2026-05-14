package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(90)
public class EvidenceGraphBuildNode implements AgentNode {

    @Override
    public String name() {
        return "EvidenceGraphBuildNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getEvidenceVerificationOutput() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        int nodeCount = context.getEvidenceVerificationOutput().getVerifiedClaims().size()
                + context.getEvidenceVerificationOutput().getRejectedClaims().size()
                + context.getEvidenceVerificationOutput().getRisks().size();
        int edgeCount = context.getEvidenceVerificationOutput().getVerifiedClaims().size()
                + context.getEvidenceVerificationOutput().getRisks().size();

        context.getAttributes().put("evidenceGraph", Map.of("nodeCount", nodeCount, "edgeCount", edgeCount));

        return AgentNodeResult.success(
                name(),
                "verificationOutput",
                "已生成证据图摘要",
                Map.of("nodeCount", nodeCount, "edgeCount", edgeCount)
        );
    }
}
