package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.InterviewQuestionOutput;
import com.techmatch.agent.output.RiskOutput;
import com.techmatch.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(100)
@RequiredArgsConstructor
public class InterviewQuestionNode implements AgentNode {

    private final InterviewService interviewService;

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
                .forEach(risk -> questions.add(buildRiskQuestion(risk)));

        if (context.getJobData() != null) {
            context.getJobData().getRequiredSkills().stream()
                    .filter(StringUtils::hasText)
                    .limit(2)
                    .forEach(skill -> questions.add(InterviewQuestionOutput.builder()
                            .type("TECH_BASIC")
                            .difficulty("中等")
                            .target(skill)
                            .question("请结合你最近一次使用 %s 的经历，说明核心原理、排障思路，以及你如何把它用在实际项目里。".formatted(skill))
                            .sourceRisk("技术栈核验")
                            .build()));
        }

        if (context.getResumeData() != null) {
            context.getResumeData().getProjects().stream()
                    .filter(project -> StringUtils.hasText(project.getName()))
                    .limit(2)
                    .forEach(project -> questions.add(InterviewQuestionOutput.builder()
                            .type("PROJECT_DEEP_DIVE")
                            .difficulty("较高")
                            .target(project.getName())
                            .question("请深入复盘项目“%s”：你的具体职责是什么，做过哪些关键取舍，以及最后产出了什么可量化结果？".formatted(project.getName()))
                            .sourceRisk("项目真实性与深度核验")
                            .build()));
        }

        String systemTarget = StringUtils.hasText(context.getJobTitle()) ? context.getJobTitle() : "目标岗位";
        questions.add(InterviewQuestionOutput.builder()
                .type("SYSTEM_DESIGN")
                .difficulty("较高")
                .target(systemTarget)
                .question("如果让你围绕“%s”设计一套可扩展方案，你会如何拆分核心模块、保证稳定性，并处理容量增长带来的挑战？".formatted(systemTarget))
                .sourceRisk("架构设计能力核验")
                .build());

        if (questions.isEmpty()) {
            questions.add(InterviewQuestionOutput.builder()
                    .type("RISK_VERIFICATION")
                    .difficulty("中等")
                    .target("综合评估")
                    .question("请挑选一段最相关的项目经历，说明它如何支撑当前岗位要求，以及有哪些结果可以证明你的贡献。")
                    .sourceRisk("综合兜底核验")
                    .build());
        }

        List<InterviewQuestionOutput> persistedQuestions = interviewService.replaceGeneratedQuestions(
                context.getTaskId(),
                context.getUserId(),
                questions.stream().distinct().toList()
        );
        context.setInterviewQuestions(persistedQuestions);

        return AgentNodeResult.success(
                name(),
                "riskOutput",
                "已生成并持久化 %d 条面试问题".formatted(persistedQuestions.size()),
                Map.of("questionCount", persistedQuestions.size())
        );
    }

    private InterviewQuestionOutput buildRiskQuestion(RiskOutput risk) {
        return InterviewQuestionOutput.builder()
                .type("RISK_VERIFICATION")
                .difficulty("中等")
                .target(StringUtils.hasText(risk.getRequirement()) ? risk.getRequirement() : risk.getTitle())
                .question("请结合具体项目或工作场景说明：%s".formatted(risk.getDetail()))
                .sourceRisk(risk.getTitle())
                .build();
    }
}
