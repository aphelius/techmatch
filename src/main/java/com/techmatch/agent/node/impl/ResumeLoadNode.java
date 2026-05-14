package com.techmatch.agent.node.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.resume.dto.StructuredResumeDto;
import com.techmatch.resume.entity.ResumeEntity;
import com.techmatch.resume.mapper.ResumeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@Order(10)
@RequiredArgsConstructor
public class ResumeLoadNode implements AgentNode {

    private final ResumeMapper resumeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return "ResumeLoadNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getResumeId() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        ResumeEntity resume = resumeMapper.selectOne(new LambdaQueryWrapper<ResumeEntity>()
                .eq(ResumeEntity::getId, context.getResumeId())
                .eq(ResumeEntity::getUserId, context.getUserId())
                .last("limit 1"));
        if (resume == null) {
            throw new BizException(ErrorCode.RESUME_NOT_FOUND);
        }
        context.setResumeFileName(resume.getFileName());
        context.setResumeRawText(resume.getRawText());
        context.setResumeData(readStructured(resume.getStructuredJson()));
        return AgentNodeResult.success(
                name(),
                "resumeId=%d".formatted(resume.getId()),
                "已加载简历文本与结构化数据",
                Map.of("fileName", resume.getFileName(), "status", resume.getStatus())
        );
    }

    private StructuredResumeDto readStructured(String json) {
        if (!StringUtils.hasText(json)) {
            return new StructuredResumeDto();
        }
        try {
            return objectMapper.readValue(json, StructuredResumeDto.class);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.RESUME_PARSE_FAILED.getCode(), exception.getMessage());
        }
    }
}
