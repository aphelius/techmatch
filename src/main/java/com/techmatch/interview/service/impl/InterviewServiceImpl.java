package com.techmatch.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techmatch.agent.output.InterviewQuestionOutput;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.interview.entity.InterviewFeedbackEntity;
import com.techmatch.interview.entity.InterviewQuestionEntity;
import com.techmatch.interview.mapper.InterviewFeedbackMapper;
import com.techmatch.interview.mapper.InterviewQuestionMapper;
import com.techmatch.interview.service.InterviewService;
import com.techmatch.match.dto.InterviewFeedbackDetailResponse;
import com.techmatch.match.dto.InterviewQuestionResponse;
import com.techmatch.task.service.AgentTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewQuestionMapper interviewQuestionMapper;
    private final InterviewFeedbackMapper interviewFeedbackMapper;
    private final AgentTaskService agentTaskService;

    @Override
    @Transactional
    public List<InterviewQuestionOutput> replaceGeneratedQuestions(Long taskId, Long userId, List<InterviewQuestionOutput> questions) {
        agentTaskService.requireOwnedTask(taskId, userId);

        List<InterviewQuestionEntity> existingQuestions = interviewQuestionMapper.selectList(new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getTaskId, taskId)
                .eq(InterviewQuestionEntity::getUserId, userId));
        if (!existingQuestions.isEmpty()) {
            List<Long> questionIds = existingQuestions.stream().map(InterviewQuestionEntity::getId).toList();
            interviewFeedbackMapper.delete(new LambdaQueryWrapper<InterviewFeedbackEntity>()
                    .eq(InterviewFeedbackEntity::getTaskId, taskId)
                    .in(InterviewFeedbackEntity::getQuestionId, questionIds));
        }

        interviewQuestionMapper.delete(new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getTaskId, taskId)
                .eq(InterviewQuestionEntity::getUserId, userId));

        LocalDateTime now = LocalDateTime.now();
        return questions.stream().map(question -> {
            InterviewQuestionEntity entity = new InterviewQuestionEntity();
            entity.setTaskId(taskId);
            entity.setUserId(userId);
            entity.setQuestionType(question.getType());
            entity.setTarget(question.getTarget());
            entity.setQuestionText(question.getQuestion());
            entity.setDifficulty(question.getDifficulty());
            entity.setSourceRisk(question.getSourceRisk());
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            interviewQuestionMapper.insert(entity);
            return InterviewQuestionOutput.builder()
                    .questionId(entity.getId())
                    .type(question.getType())
                    .question(question.getQuestion())
                    .target(question.getTarget())
                    .difficulty(question.getDifficulty())
                    .sourceRisk(question.getSourceRisk())
                    .build();
        }).toList();
    }

    @Override
    public List<InterviewQuestionResponse> listQuestions(Long taskId, Long userId) {
        agentTaskService.requireOwnedTask(taskId, userId);

        List<InterviewQuestionEntity> questions = interviewQuestionMapper.selectList(new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getTaskId, taskId)
                .eq(InterviewQuestionEntity::getUserId, userId)
                .orderByAsc(InterviewQuestionEntity::getId));
        List<InterviewFeedbackEntity> feedbackList = interviewFeedbackMapper.selectList(new LambdaQueryWrapper<InterviewFeedbackEntity>()
                .eq(InterviewFeedbackEntity::getTaskId, taskId)
                .eq(InterviewFeedbackEntity::getUserId, userId)
                .orderByDesc(InterviewFeedbackEntity::getUpdateTime));

        Map<Long, InterviewFeedbackEntity> feedbackByQuestionId = feedbackList.stream()
                .collect(Collectors.toMap(InterviewFeedbackEntity::getQuestionId, Function.identity(), (left, right) -> left));

        return questions.stream().map(question -> InterviewQuestionResponse.builder()
                        .questionId(question.getId())
                        .type(question.getQuestionType())
                        .question(question.getQuestionText())
                        .target(question.getTarget())
                        .difficulty(question.getDifficulty())
                        .sourceRisk(question.getSourceRisk())
                        .feedback(toFeedbackResponse(feedbackByQuestionId.get(question.getId())))
                        .build())
                .toList();
    }

    @Override
    public InterviewQuestionEntity requireOwnedQuestion(Long taskId, Long userId, Long questionId) {
        InterviewQuestionEntity entity = interviewQuestionMapper.selectOne(new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getId, questionId)
                .eq(InterviewQuestionEntity::getTaskId, taskId)
                .eq(InterviewQuestionEntity::getUserId, userId)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
        return entity;
    }

    private InterviewFeedbackDetailResponse toFeedbackResponse(InterviewFeedbackEntity entity) {
        if (entity == null) {
            return null;
        }
        return InterviewFeedbackDetailResponse.builder()
                .feedbackId(entity.getId())
                .questionId(entity.getQuestionId())
                .score(entity.getScore())
                .feedbackType(entity.getFeedbackType())
                .notes(entity.getNotes())
                .confidenceDelta(entity.getConfidenceDelta())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
