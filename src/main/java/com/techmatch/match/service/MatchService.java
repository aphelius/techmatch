package com.techmatch.match.service;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.output.FinalReportOutput;
import com.techmatch.agent.orchestrator.MatchAgentOrchestrator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techmatch.auth.security.LoginUserContext;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.graph.dto.EvidenceGraphResponse;
import com.techmatch.graph.service.EvidenceGraphService;
import com.techmatch.job.entity.JobDescriptionEntity;
import com.techmatch.job.mapper.JobDescriptionMapper;
import com.techmatch.match.dto.CreateMatchRequest;
import com.techmatch.match.dto.CreateMatchResponse;
import com.techmatch.match.dto.MatchEvidenceGraphResponse;
import com.techmatch.match.dto.MatchReportResponse;
import com.techmatch.resume.entity.ResumeEntity;
import com.techmatch.resume.mapper.ResumeMapper;
import com.techmatch.task.dto.AgentTaskResponse;
import com.techmatch.task.dto.AgentTimelineEventResponse;
import com.techmatch.task.entity.AgentTaskEntity;
import com.techmatch.task.service.AgentTaskService;
import com.techmatch.task.service.AgentTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final LoginUserContext loginUserContext;
    private final ResumeMapper resumeMapper;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final AgentTaskService agentTaskService;
    private final AgentTimelineService agentTimelineService;
    private final MatchAgentOrchestrator matchAgentOrchestrator;
    private final EvidenceGraphService evidenceGraphService;

    public CreateMatchResponse createMatchTask(CreateMatchRequest request) {
        Long userId = loginUserContext.getCurrentUserId();
        ResumeEntity resume = requireCompletedResume(userId, request.getResumeId());
        JobDescriptionEntity job = requireCompletedJob(userId, request.getJobDescriptionId());

        AgentTaskEntity task = agentTaskService.createMatchTask(userId, resume.getId(), job.getId());
        agentTimelineService.recordCreated(
                task.getId(),
                "Match task created for resumeId=%d and jobDescriptionId=%d".formatted(resume.getId(), job.getId())
        );

        try {
            matchAgentOrchestrator.run(buildContext(task));
        } catch (RuntimeException exception) {
            log.warn("Match task execution finished with failure: taskId={}, message={}", task.getId(), exception.getMessage());
        }

        AgentTaskResponse currentTask = agentTaskService.getTaskResponse(task.getId(), userId);
        return CreateMatchResponse.builder()
                .taskId(currentTask.getTaskId())
                .resumeId(currentTask.getResumeId())
                .jobDescriptionId(currentTask.getJobDescriptionId())
                .taskType(currentTask.getTaskType())
                .status(currentTask.getStatus())
                .currentNode(currentTask.getCurrentNode())
                .createTime(currentTask.getCreateTime())
                .build();
    }

    public AgentTaskResponse getTask(Long taskId) {
        Long userId = loginUserContext.getCurrentUserId();
        return agentTaskService.getTaskResponse(taskId, userId);
    }

    public List<AgentTimelineEventResponse> getTimeline(Long taskId) {
        Long userId = loginUserContext.getCurrentUserId();
        return agentTimelineService.listTimeline(taskId, userId);
    }

    public MatchReportResponse getReport(Long taskId) {
        Long userId = loginUserContext.getCurrentUserId();
        FinalReportOutput report = agentTaskService.getFinalReport(taskId, userId);
        return MatchReportResponse.builder()
                .taskId(report.getTaskId())
                .resumeId(report.getResumeId())
                .jobDescriptionId(report.getJobDescriptionId())
                .candidateName(report.getCandidateName())
                .jobTitle(report.getJobTitle())
                .totalScore(report.getTotalScore())
                .confidence(report.getConfidence())
                .matchLevel(report.getMatchLevel())
                .recommendation(report.getRecommendation())
                .summary(report.getSummary())
                .dimensionScores(report.getDimensionScores())
                .strengths(report.getStrengths())
                .risks(report.getRisks())
                .suggestions(report.getSuggestions())
                .interviewQuestions(report.getInterviewQuestions())
                .build();
    }

    public MatchEvidenceGraphResponse getEvidenceGraph(Long taskId) {
        Long userId = loginUserContext.getCurrentUserId();
        EvidenceGraphResponse graph = evidenceGraphService.getGraph(taskId, userId);
        return MatchEvidenceGraphResponse.builder()
                .taskId(graph.getTaskId())
                .nodes(graph.getNodes())
                .edges(graph.getEdges())
                .chains(graph.getChains())
                .build();
    }

    private MatchAgentContext buildContext(AgentTaskEntity task) {
        MatchAgentContext context = new MatchAgentContext();
        context.setTaskId(task.getId());
        context.setUserId(task.getUserId());
        context.setResumeId(task.getResumeId());
        context.setJobDescriptionId(task.getJobDescriptionId());
        return context;
    }

    private ResumeEntity requireCompletedResume(Long userId, Long resumeId) {
        ResumeEntity entity = resumeMapper.selectOne(new LambdaQueryWrapper<ResumeEntity>()
                .eq(ResumeEntity::getId, resumeId)
                .eq(ResumeEntity::getUserId, userId)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.RESUME_NOT_FOUND);
        }
        if (!"COMPLETED".equalsIgnoreCase(entity.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "resume is not ready for matching");
        }
        return entity;
    }

    private JobDescriptionEntity requireCompletedJob(Long userId, Long jobDescriptionId) {
        JobDescriptionEntity entity = jobDescriptionMapper.selectOne(new LambdaQueryWrapper<JobDescriptionEntity>()
                .eq(JobDescriptionEntity::getId, jobDescriptionId)
                .eq(JobDescriptionEntity::getUserId, userId)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.JOB_NOT_FOUND);
        }
        if (!"COMPLETED".equalsIgnoreCase(entity.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "job description is not ready for matching");
        }
        return entity;
    }
}
