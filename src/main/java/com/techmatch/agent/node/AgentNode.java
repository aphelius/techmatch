package com.techmatch.agent.node;

import com.techmatch.agent.context.MatchAgentContext;

public interface AgentNode {

    String name();

    boolean shouldExecute(MatchAgentContext context);

    AgentNodeResult execute(MatchAgentContext context);
}
