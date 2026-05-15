package com.techmatch.graph.service;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.graph.dto.EvidenceGraphResponse;
import com.techmatch.interview.entity.InterviewFeedbackEntity;
import com.techmatch.interview.entity.InterviewQuestionEntity;

public interface EvidenceGraphService {

    void rebuild(MatchAgentContext context);

    void appendFeedback(Long taskId, Long userId, InterviewQuestionEntity question, InterviewFeedbackEntity feedback);

    EvidenceGraphResponse getGraph(Long taskId, Long userId);
}
