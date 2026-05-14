package com.techmatch.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.auth.security.LoginUserContext;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.job.dto.CreateJobRequest;
import com.techmatch.job.dto.JobChunkResponse;
import com.techmatch.job.dto.JobCreateResponse;
import com.techmatch.job.dto.JobDetailResponse;
import com.techmatch.job.dto.JobListItemResponse;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.job.entity.JobDescriptionChunkEntity;
import com.techmatch.job.entity.JobDescriptionEntity;
import com.techmatch.job.mapper.JobDescriptionMapper;
import com.techmatch.job.repository.JobDescriptionChunkRepository;
import com.techmatch.job.service.parser.JobParserNode;
import com.techmatch.resume.service.embedding.EmbeddingService;
import com.techmatch.resume.service.splitter.ResumeTextSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobDescriptionMapper jobDescriptionMapper;
    private final JobDescriptionChunkRepository jobDescriptionChunkRepository;
    private final LoginUserContext loginUserContext;
    private final JobParserNode jobParserNode;
    private final ResumeTextSplitter resumeTextSplitter;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Transactional
    public JobCreateResponse create(CreateJobRequest request) {
        Long userId = loginUserContext.getCurrentUserId();
        validateRequest(request);

        JobStructuredData structured = jobParserNode.parse(request);
        LocalDateTime now = LocalDateTime.now();

        JobDescriptionEntity entity = new JobDescriptionEntity();
        entity.setUserId(userId);
        entity.setTitle(request.getTitle().trim());
        entity.setCompany(trimToNull(request.getCompany()));
        entity.setLocation(trimToNull(request.getLocation()));
        entity.setRawText(request.getRawText().trim());
        entity.setStructuredJson(writeJson(structured));
        entity.setStatus("COMPLETED");
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        jobDescriptionMapper.insert(entity);

        List<ResumeTextSplitter.SplitChunk> chunks = resumeTextSplitter.split(entity.getRawText());
        for (ResumeTextSplitter.SplitChunk chunk : chunks) {
            jobDescriptionChunkRepository.insert(toChunkEntity(entity.getId(), userId, chunk));
        }

        log.info("Job description created: userId={}, jobId={}, chunkCount={}", userId, entity.getId(), chunks.size());
        return JobCreateResponse.builder()
                .jobId(entity.getId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .chunkCount(chunks.size())
                .build();
    }

    public List<JobListItemResponse> listMine() {
        Long userId = loginUserContext.getCurrentUserId();
        return jobDescriptionMapper.selectList(new LambdaQueryWrapper<JobDescriptionEntity>()
                        .eq(JobDescriptionEntity::getUserId, userId)
                        .orderByDesc(JobDescriptionEntity::getCreateTime))
                .stream()
                .map(this::toListItem)
                .toList();
    }

    public JobDetailResponse getDetail(Long jobId) {
        Long userId = loginUserContext.getCurrentUserId();
        JobDescriptionEntity entity = jobDescriptionMapper.selectOne(new LambdaQueryWrapper<JobDescriptionEntity>()
                .eq(JobDescriptionEntity::getId, jobId)
                .eq(JobDescriptionEntity::getUserId, userId)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "job description not found");
        }

        List<JobChunkResponse> chunks = jobDescriptionChunkRepository.findByJobDescriptionIdAndUserId(jobId, userId)
                .stream()
                .map(chunk -> JobChunkResponse.builder()
                        .chunkId(chunk.getId())
                        .chunkIndex(chunk.getChunkIndex())
                        .chunkText(chunk.getChunkText())
                        .metadataJson(chunk.getMetadataJson())
                        .embedding(chunk.getEmbedding())
                        .embeddingDimensions(chunk.getEmbedding().size())
                        .build())
                .toList();

        return JobDetailResponse.builder()
                .jobId(entity.getId())
                .title(entity.getTitle())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .status(entity.getStatus())
                .rawText(entity.getRawText())
                .structured(readStructured(entity.getStructuredJson()))
                .parseError(entity.getParseError())
                .createTime(entity.getCreateTime())
                .chunks(chunks)
                .build();
    }

    private void validateRequest(CreateJobRequest request) {
        if (request == null) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "request is required");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "title is required");
        }
        if (!StringUtils.hasText(request.getRawText())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "rawText is required");
        }
    }

    private JobDescriptionChunkEntity toChunkEntity(Long jobId,
                                                    Long userId,
                                                    ResumeTextSplitter.SplitChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("startOffset", chunk.getStartOffset());
        metadata.put("endOffset", chunk.getEndOffset());
        metadata.put("length", chunk.getText().length());

        JobDescriptionChunkEntity entity = new JobDescriptionChunkEntity();
        entity.setJobDescriptionId(jobId);
        entity.setUserId(userId);
        entity.setChunkIndex(chunk.getChunkIndex());
        entity.setChunkText(chunk.getText());
        entity.setMetadataJson(writeJson(metadata));
        entity.setEmbedding(embeddingService.embed(chunk.getText()));
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

    private JobListItemResponse toListItem(JobDescriptionEntity entity) {
        return JobListItemResponse.builder()
                .jobId(entity.getId())
                .title(entity.getTitle())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

    private JobStructuredData readStructured(String structuredJson) {
        if (!StringUtils.hasText(structuredJson)) {
            return new JobStructuredData();
        }
        try {
            return objectMapper.readValue(structuredJson, JobStructuredData.class);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), exception.getMessage());
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), exception.getMessage());
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
