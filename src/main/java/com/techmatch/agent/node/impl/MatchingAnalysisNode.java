package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.MatchAnalysisOutput;
import com.techmatch.agent.output.MissingRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(70)
@RequiredArgsConstructor
public class MatchingAnalysisNode implements AgentNode {

    @Override
    public String name() {
        return "MatchingAnalysisNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getHybridScoreResult() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        BigDecimal totalScore = context.getHybridScoreResult().getTotalScore();
        String matchLevel = resolveMatchLevel(totalScore);
        String recommendation = resolveRecommendation(totalScore);

        List<String> strengths = new ArrayList<>();
        context.getHybridScoreResult().getMatchedRequirements().stream()
                .limit(3)
                .forEach(item -> strengths.add("命中要求“%s”，证据来自简历片段".formatted(item.getRequirement())));
        if (strengths.isEmpty()) {
            strengths.add("暂无明确强匹配项，建议结合面试进一步核验");
        }

        List<String> risks = new ArrayList<>();
        context.getHybridScoreResult().getMissingRequirements().stream()
                .limit(3)
                .map(MissingRequirement::getRequirement)
                .forEach(item -> risks.add("必备要求“%s”缺少明确证据".formatted(item)));
        context.getWarnings().forEach(risks::add);

        List<String> suggestions = new ArrayList<>();
        suggestions.add("优先围绕缺失技能和薄弱项目证据设计面试追问");
        suggestions.add("如候选人确有相关经验，建议补充更量化的项目结果与职责描述");

        MatchAnalysisOutput output = MatchAnalysisOutput.builder()
                .matchLevel(matchLevel)
                .recommendation(recommendation)
                .summary("综合得分 %s，当前判断为%s".formatted(totalScore, matchLevel))
                .strengths(strengths)
                .risks(risks)
                .suggestions(suggestions)
                .build();
        context.setMatchAnalysisOutput(output);

        return AgentNodeResult.success(
                name(),
                "hybridScoreResult",
                "匹配分析结论已生成",
                Map.of("matchLevel", matchLevel, "riskCount", risks.size())
        );
    }

    private String resolveMatchLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "强匹配";
        }
        if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "中等匹配";
        }
        return "弱匹配";
    }

    private String resolveRecommendation(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "建议优先安排面试";
        }
        if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "建议补充核验后进入面试";
        }
        return "建议谨慎推进";
    }
}
