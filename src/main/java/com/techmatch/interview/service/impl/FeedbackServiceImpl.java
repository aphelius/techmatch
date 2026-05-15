package com.techmatch.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techmatch.agent.output.DimensionScore;
import com.techmatch.agent.output.FinalReportOutput;
import com.techmatch.agent.output.InterviewFeedbackSummary;
import com.techmatch.graph.service.EvidenceGraphService;
import com.techmatch.interview.entity.InterviewFeedbackEntity;
import com.techmatch.interview.entity.InterviewQuestionEntity;
import com.techmatch.interview.mapper.InterviewFeedbackMapper;
import com.techmatch.interview.mapper.InterviewQuestionMapper;
import com.techmatch.interview.service.FeedbackService;
import com.techmatch.interview.service.InterviewService;
import com.techmatch.match.dto.SubmitInterviewFeedbackRequest;
import com.techmatch.match.dto.SubmitInterviewFeedbackResponse;
import com.techmatch.task.service.AgentTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private static final BigDecimal MIN_CONFIDENCE = BigDecimal.valueOf(0.35);
    private static final BigDecimal MAX_CONFIDENCE = BigDecimal.valueOf(0.99);

    private final InterviewFeedbackMapper interviewFeedbackMapper;
    private final InterviewQuestionMapper interviewQuestionMapper;
    private final InterviewService interviewService;
    private final AgentTaskService agentTaskService;
    private final EvidenceGraphService evidenceGraphService;

    @Override
    @Transactional
    public SubmitInterviewFeedbackResponse submit(Long taskId, Long userId, SubmitInterviewFeedbackRequest request) {
        agentTaskService.requireOwnedTask(taskId, userId);
        InterviewQuestionEntity question = interviewService.requireOwnedQuestion(taskId, userId, request.getQuestionId());

        InterviewFeedbackEntity entity = interviewFeedbackMapper.selectOne(new LambdaQueryWrapper<InterviewFeedbackEntity>()
                .eq(InterviewFeedbackEntity::getTaskId, taskId)
                .eq(InterviewFeedbackEntity::getQuestionId, question.getId())
                .eq(InterviewFeedbackEntity::getUserId, userId)
                .last("limit 1"));

        LocalDateTime now = LocalDateTime.now();
        if (entity == null) {
            entity = new InterviewFeedbackEntity();
            entity.setTaskId(taskId);
            entity.setQuestionId(question.getId());
            entity.setUserId(userId);
            entity.setCreateTime(now);
        }
        entity.setScore(request.getScore());
        entity.setFeedbackType(request.getFeedbackType());
        entity.setNotes(StringUtils.hasText(request.getNotes()) ? request.getNotes().trim() : null);
        entity.setConfidenceDelta(resolveConfidenceDelta(request.getScore(), request.getFeedbackType()));
        entity.setUpdateTime(now);

        if (entity.getId() == null) {
            interviewFeedbackMapper.insert(entity);
        } else {
            interviewFeedbackMapper.updateById(entity);
        }

        FinalReportOutput updatedReport = rebuildReportWithFeedback(taskId, userId);
        evidenceGraphService.appendFeedback(taskId, userId, question, entity);

        return SubmitInterviewFeedbackResponse.builder()
                .taskId(taskId)
                .questionId(question.getId())
                .feedbackId(entity.getId())
                .confidence(updatedReport.getConfidence())
                .matchLevel(updatedReport.getMatchLevel())
                .recommendation(updatedReport.getRecommendation())
                .feedbackSummary(updatedReport.getFeedbackSummary())
                .build();
    }

    private FinalReportOutput rebuildReportWithFeedback(Long taskId, Long userId) {
        FinalReportOutput report = agentTaskService.getFinalReport(taskId, userId);
        List<InterviewFeedbackEntity> feedbackList = interviewFeedbackMapper.selectList(new LambdaQueryWrapper<InterviewFeedbackEntity>()
                .eq(InterviewFeedbackEntity::getTaskId, taskId)
                .eq(InterviewFeedbackEntity::getUserId, userId)
                .orderByAsc(InterviewFeedbackEntity::getId));
        List<InterviewQuestionEntity> questions = interviewQuestionMapper.selectList(new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getTaskId, taskId)
                .eq(InterviewQuestionEntity::getUserId, userId)
                .orderByAsc(InterviewQuestionEntity::getId));

        BigDecimal baseConfidence = report.getBaseConfidence() != null ? report.getBaseConfidence() : report.getConfidence();
        String baseMatchLevel = StringUtils.hasText(report.getBaseMatchLevel()) ? report.getBaseMatchLevel() : report.getMatchLevel();
        String baseRecommendation = StringUtils.hasText(report.getBaseRecommendation()) ? report.getBaseRecommendation() : report.getRecommendation();
        List<DimensionScore> baseDimensionScores = report.getBaseDimensionScores() != null && !report.getBaseDimensionScores().isEmpty()
                ? report.getBaseDimensionScores()
                : report.getDimensionScores();

        BigDecimal averageScore = feedbackList.stream()
                .map(InterviewFeedbackEntity::getScore)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, feedbackList.size())), 4, RoundingMode.HALF_UP);
        BigDecimal averageDelta = feedbackList.stream()
                .map(InterviewFeedbackEntity::getConfidenceDelta)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, feedbackList.size())), 4, RoundingMode.HALF_UP);
        BigDecimal coverage = questions.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(feedbackList.size())
                .divide(BigDecimal.valueOf(questions.size()), 4, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);
        BigDecimal aggregateDelta = averageDelta.multiply(BigDecimal.valueOf(0.5).add(coverage.multiply(BigDecimal.valueOf(0.5))));
        BigDecimal adjustedConfidence = clamp(baseConfidence.add(aggregateDelta), MIN_CONFIDENCE, MAX_CONFIDENCE);
        BigDecimal adjustedScore = report.getTotalScore().add(
                averageScore.subtract(BigDecimal.valueOf(3))
                        .multiply(BigDecimal.valueOf(4))
                        .multiply(coverage)
        );

        String adjustedMatchLevel = resolveMatchLevel(adjustedScore);
        String adjustedRecommendation = resolveRecommendation(adjustedScore, averageScore);
        List<DimensionScore> adjustedDimensionScores = rebuildDimensionScores(baseDimensionScores, feedbackList, questions);

        InterviewFeedbackSummary feedbackSummary = feedbackList.isEmpty() ? null : InterviewFeedbackSummary.builder()
                .feedbackCount(feedbackList.size())
                .averageScore(averageScore.setScale(2, RoundingMode.HALF_UP))
                .confidenceDelta(aggregateDelta.setScale(4, RoundingMode.HALF_UP))
                .adjustedMatchLevel(adjustedMatchLevel)
                .adjustedRecommendation(adjustedRecommendation)
                .note(buildFeedbackNote(averageScore, coverage))
                .build();

        FinalReportOutput updatedReport = FinalReportOutput.builder()
                .taskId(report.getTaskId())
                .resumeId(report.getResumeId())
                .jobDescriptionId(report.getJobDescriptionId())
                .candidateName(report.getCandidateName())
                .jobTitle(report.getJobTitle())
                .totalScore(report.getTotalScore())
                .confidence(adjustedConfidence.setScale(4, RoundingMode.HALF_UP))
                .matchLevel(feedbackList.isEmpty() ? baseMatchLevel : adjustedMatchLevel)
                .recommendation(feedbackList.isEmpty() ? baseRecommendation : adjustedRecommendation)
                .baseConfidence(baseConfidence)
                .baseMatchLevel(baseMatchLevel)
                .baseRecommendation(baseRecommendation)
                .summary(report.getSummary())
                .dimensionScores(feedbackList.isEmpty() ? baseDimensionScores : adjustedDimensionScores)
                .baseDimensionScores(baseDimensionScores)
                .strengths(report.getStrengths())
                .risks(report.getRisks())
                .suggestions(report.getSuggestions())
                .interviewQuestions(report.getInterviewQuestions())
                .feedbackSummary(feedbackSummary)
                .build();
        agentTaskService.saveFinalReport(taskId, updatedReport);
        return updatedReport;
    }

    private List<DimensionScore> rebuildDimensionScores(List<DimensionScore> baseScores,
                                                        List<InterviewFeedbackEntity> feedbackList,
                                                        List<InterviewQuestionEntity> questions) {
        Map<Long, InterviewQuestionEntity> questionMap = questions.stream()
                .collect(Collectors.toMap(InterviewQuestionEntity::getId, Function.identity(), (left, right) -> left));

        return baseScores.stream().map(score -> {
            BigDecimal delta = feedbackList.stream()
                    .filter(feedback -> {
                        InterviewQuestionEntity question = questionMap.get(feedback.getQuestionId());
                        return question != null && dimensionMatches(score.getDimension(), question.getQuestionType());
                    })
                    .map(InterviewFeedbackEntity::getConfidenceDelta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long relatedCount = feedbackList.stream()
                    .filter(feedback -> {
                        InterviewQuestionEntity question = questionMap.get(feedback.getQuestionId());
                        return question != null && dimensionMatches(score.getDimension(), question.getQuestionType());
                    })
                    .count();
            BigDecimal normalizedDelta = relatedCount == 0
                    ? BigDecimal.ZERO
                    : delta.divide(BigDecimal.valueOf(relatedCount), 4, RoundingMode.HALF_UP);
            BigDecimal nextConfidence = clamp(score.getConfidence().add(normalizedDelta), BigDecimal.valueOf(0.35), BigDecimal.valueOf(0.98));
            return DimensionScore.builder()
                    .dimension(score.getDimension())
                    .score(score.getScore())
                    .maxScore(score.getMaxScore())
                    .confidence(nextConfidence.setScale(4, RoundingMode.HALF_UP))
                    .reason(score.getReason())
                    .build();
        }).toList();
    }

    private BigDecimal resolveConfidenceDelta(Integer score, String feedbackType) {
        BigDecimal scoreDelta = BigDecimal.valueOf(score - 3L).multiply(BigDecimal.valueOf(0.045));
        BigDecimal typeBias = switch (feedbackType) {
            case "证据充分", "技术扎实" -> BigDecimal.valueOf(0.03);
            case "需要追问" -> BigDecimal.valueOf(-0.02);
            case "表现偏弱" -> BigDecimal.valueOf(-0.05);
            case "未验证" -> BigDecimal.valueOf(-0.06);
            default -> BigDecimal.ZERO;
        };
        return scoreDelta.add(typeBias).setScale(4, RoundingMode.HALF_UP);
    }

    private String resolveMatchLevel(BigDecimal adjustedScore) {
        if (adjustedScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "强匹配";
        }
        if (adjustedScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "中等匹配";
        }
        return "弱匹配";
    }

    private String resolveRecommendation(BigDecimal adjustedScore, BigDecimal averageScore) {
        if (adjustedScore.compareTo(BigDecimal.valueOf(80)) >= 0 && averageScore.compareTo(BigDecimal.valueOf(4)) >= 0) {
            return "建议优先推进后续面试";
        }
        if (adjustedScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            if (averageScore.compareTo(BigDecimal.valueOf(3)) >= 0) {
                return "建议结合追问结果继续推进";
            }
            return "建议补充验证后再决定是否推进";
        }
        return "建议谨慎推进";
    }

    private String buildFeedbackNote(BigDecimal averageScore, BigDecimal coverage) {
        if (averageScore.compareTo(BigDecimal.valueOf(4)) >= 0) {
            return "面试反馈整体积极，提升了候选人与岗位匹配结论的可信度";
        }
        if (averageScore.compareTo(BigDecimal.valueOf(2.5)) < 0) {
            return "面试反馈暴露出明显风险，系统已下调置信度并建议谨慎推进";
        }
        if (coverage.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            return "当前反馈覆盖的问题还不够多，建议继续完成剩余关键问题核验";
        }
        return "面试反馈与既有证据基本一致，建议结合后续追问继续判断";
    }

    private boolean dimensionMatches(String dimension, String questionType) {
        return switch (questionType) {
            case "TECH_BASIC", "RISK_VERIFICATION" -> "TECH_SKILL".equals(dimension);
            case "PROJECT_DEEP_DIVE", "SYSTEM_DESIGN" -> "PROJECT_EXPERIENCE".equals(dimension);
            default -> false;
        };
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }
}
