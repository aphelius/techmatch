package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@Order(40)
public class JDQualityCheckNode implements AgentNode {

    @Override
    public String name() {
        return "JDQualityCheckNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return StringUtils.hasText(context.getJdRawText());
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        String quality;
        if (context.getJobData().getRequiredSkills().isEmpty()) {
            context.getWarnings().add("岗位未提炼出 requiredSkills，匹配结果会更依赖文本语义");
            quality = "LOW";
        } else if (context.getJobData().getResponsibilities().isEmpty()) {
            quality = "MEDIUM";
        } else {
            quality = "HIGH";
        }
        context.getAttributes().put("jobQuality", quality);

        return AgentNodeResult.success(
                name(),
                "jobRawText",
                "岗位质量检查完成",
                Map.of("quality", quality, "requiredSkillCount", context.getJobData().getRequiredSkills().size())
        );
    }
}
