package com.techmatch.agent.node.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.job.entity.JobDescriptionEntity;
import com.techmatch.job.mapper.JobDescriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@Order(20)
@RequiredArgsConstructor
public class JobLoadNode implements AgentNode {

    private final JobDescriptionMapper jobDescriptionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return "JobLoadNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getJobDescriptionId() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        JobDescriptionEntity job = jobDescriptionMapper.selectOne(new LambdaQueryWrapper<JobDescriptionEntity>()
                .eq(JobDescriptionEntity::getId, context.getJobDescriptionId())
                .eq(JobDescriptionEntity::getUserId, context.getUserId())
                .last("limit 1"));
        if (job == null) {
            throw new BizException(ErrorCode.JOB_NOT_FOUND);
        }
        context.setJobTitle(job.getTitle());
        context.setJdRawText(job.getRawText());
        context.setJobData(readStructured(job.getStructuredJson()));
        return AgentNodeResult.success(
                name(),
                "jobDescriptionId=%d".formatted(job.getId()),
                "已加载岗位文本与结构化要求",
                Map.of("title", job.getTitle(), "status", job.getStatus())
        );
    }

    private JobStructuredData readStructured(String json) {
        if (!StringUtils.hasText(json)) {
            return new JobStructuredData();
        }
        try {
            return objectMapper.readValue(json, JobStructuredData.class);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), exception.getMessage());
        }
    }
}
