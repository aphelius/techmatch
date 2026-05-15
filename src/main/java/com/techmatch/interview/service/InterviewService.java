package com.techmatch.interview.service;

import com.techmatch.agent.output.InterviewQuestionOutput;
import com.techmatch.interview.entity.InterviewQuestionEntity;
import com.techmatch.match.dto.InterviewQuestionResponse;

import java.util.List;

public interface InterviewService {

    List<InterviewQuestionOutput> replaceGeneratedQuestions(Long taskId, Long userId, List<InterviewQuestionOutput> questions);

    List<InterviewQuestionResponse> listQuestions(Long taskId, Long userId);

    InterviewQuestionEntity requireOwnedQuestion(Long taskId, Long userId, Long questionId);
}
