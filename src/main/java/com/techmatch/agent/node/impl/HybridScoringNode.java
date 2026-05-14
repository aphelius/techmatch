package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.HybridScoreResult;
import com.techmatch.scoring.engine.HybridScoringEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(60)
@RequiredArgsConstructor
public class HybridScoringNode implements AgentNode {

    private final HybridScoringEngine hybridScoringEngine;

    @Override
    public String name() {
        return "HybridScoringNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getResumeData() != null && context.getJobData() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        HybridScoreResult result = hybridScoringEngine.score(
                context.getResumeData(),
                context.getJobData(),
                context.getRetrievedChunks(),
                context.getWarnings()
        );
        context.setHybridScoreResult(result);

        return AgentNodeResult.success(
                name(),
                "structuredResume + structuredJob + retrievedChunks",
                "已生成 Hybrid Score，总分 %s".formatted(result.getTotalScore()),
                Map.of(
                        "totalScore", result.getTotalScore(),
                        "confidence", result.getConfidence(),
                        "dimensionCount", result.getDimensionScores().size()
                )
        );
    }
}
