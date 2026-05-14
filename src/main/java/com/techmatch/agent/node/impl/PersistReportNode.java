package com.techmatch.agent.node.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.task.service.AgentTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(120)
@RequiredArgsConstructor
public class PersistReportNode implements AgentNode {

    private final ObjectMapper objectMapper;
    private final AgentTaskService agentTaskService;

    @Override
    public String name() {
        return "PersistReportNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getFinalReport() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        String reportJson = writeReport(context);
        context.getAttributes().put("finalReportJson", reportJson);
        agentTaskService.saveFinalReport(context.getTaskId(), context.getFinalReport());

        return AgentNodeResult.success(
                name(),
                "finalReport",
                "报告已写入任务记录，可供前端直接查询",
                Map.of("reportSize", reportJson.length())
        );
    }

    private String writeReport(MatchAgentContext context) {
        try {
            return objectMapper.writeValueAsString(context.getFinalReport());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }
}
