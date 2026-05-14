package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.InterviewQuestionOutput;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(100)
public class InterviewQuestionNode implements AgentNode {

    @Override
    public String name() {
        return "InterviewQuestionNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getEvidenceVerificationOutput() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        List<InterviewQuestionOutput> questions = new ArrayList<>();

        context.getEvidenceVerificationOutput().getRisks().stream()
                .limit(3)
                .forEach(risk -> questions.add(InterviewQuestionOutput.builder()
                        .type("风险验证题")
                        .difficulty("中等")
                        .target(risk.getRequirement() == null ? risk.getTitle() : risk.getRequirement())
                        .question("请结合具体项目说明：%s".formatted(risk.getDetail()))
                        .build()));

        if (questions.isEmpty()) {
            questions.add(InterviewQuestionOutput.builder()
                    .type("综合题")
                    .difficulty("中等")
                    .target("综合评估")
                    .question("请挑选一段最相关项目经历，说明它如何支撑当前岗位要求。")
                    .build());
        }

        context.setInterviewQuestions(questions);

        return AgentNodeResult.success(
                name(),
                "riskOutput",
                "已生成 %d 条面试问题".formatted(questions.size()),
                Map.of("questionCount", questions.size())
        );
    }
}
