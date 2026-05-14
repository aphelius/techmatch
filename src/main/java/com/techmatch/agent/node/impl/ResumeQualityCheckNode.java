package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@Order(30)
public class ResumeQualityCheckNode implements AgentNode {

    @Override
    public String name() {
        return "ResumeQualityCheckNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return StringUtils.hasText(context.getResumeRawText());
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        String quality;
        int textLength = context.getResumeRawText().length();
        if (textLength < 300) {
            context.getWarnings().add("简历文本内容较短，分析置信度会降低");
            quality = "LOW";
        } else if (textLength < 800) {
            quality = "MEDIUM";
        } else {
            quality = "HIGH";
        }
        context.getAttributes().put("resumeQuality", quality);

        if (context.getResumeData().getSkills().isEmpty()) {
            context.getWarnings().add("简历未提炼出技能标签，技能匹配结果可能偏保守");
        }

        return AgentNodeResult.success(
                name(),
                "resumeRawText",
                "简历质量检查完成",
                Map.of("quality", quality, "warnings", context.getWarnings().size())
        );
    }
}
