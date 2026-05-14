package com.techmatch.resume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatch.auth.security.LoginUserContext;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.config.ResumeProperties;
import com.techmatch.resume.dto.ResumeChunkResponse;
import com.techmatch.resume.dto.ResumeChunkSearchResponse;
import com.techmatch.resume.dto.ResumeDetailResponse;
import com.techmatch.resume.dto.ResumeListItemResponse;
import com.techmatch.resume.dto.ResumeUploadResponse;
import com.techmatch.resume.dto.StructuredResumeDto;
import com.techmatch.resume.entity.DocumentChunkEntity;
import com.techmatch.resume.entity.ResumeEntity;
import com.techmatch.resume.mapper.ResumeMapper;
import com.techmatch.resume.repository.PgvectorDocumentChunkRepository;
import com.techmatch.resume.service.embedding.EmbeddingService;
import com.techmatch.resume.service.extract.ResumeContentExtractor;
import com.techmatch.resume.service.parser.ResumeParserNode;
import com.techmatch.resume.service.splitter.ResumeTextSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeMapper resumeMapper;
    private final PgvectorDocumentChunkRepository documentChunkRepository;
    private final LoginUserContext loginUserContext;
    private final ResumeProperties resumeProperties;
    private final MinioService minioService;
    private final ResumeContentExtractor resumeContentExtractor;
    private final ResumeParserNode resumeParserNode;
    private final ResumeTextSplitter resumeTextSplitter;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResumeUploadResponse upload(MultipartFile file) {
        validateFile(file);

        Long userId = loginUserContext.getCurrentUserId();
        String fileName = resolveFileName(file);
        String extension = extensionOf(fileName);
        byte[] content = readContent(file);
        String objectKey = buildObjectKey(userId, extension);
        String storageUrl = minioService.upload(objectKey, content, file.getContentType());

        String rawText = resumeContentExtractor.extract(extension, content);
        StructuredResumeDto structured = resumeParserNode.parse(rawText, fileName);
        String structuredJson = writeJson(structured);

        LocalDateTime now = LocalDateTime.now();
        ResumeEntity resume = new ResumeEntity();
        resume.setUserId(userId);
        resume.setFileName(fileName);
        resume.setFileType(extension);
        resume.setFileSize(file.getSize());
        resume.setStorageBucket(minioService.getBucket());
        resume.setStorageObjectKey(objectKey);
        resume.setStorageUrl(storageUrl);
        resume.setStatus("COMPLETED");
        resume.setRawText(rawText);
        resume.setStructuredJson(structuredJson);
        resume.setCreateTime(now);
        resume.setUpdateTime(now);
        resumeMapper.insert(resume);

        List<ResumeTextSplitter.SplitChunk> chunks = resumeTextSplitter.split(rawText);
        for (ResumeTextSplitter.SplitChunk chunk : chunks) {
            documentChunkRepository.insert(toChunkEntity(resume.getId(), userId, chunk));
        }

        log.info("Resume upload completed: userId={}, resumeId={}, chunkCount={}", userId, resume.getId(), chunks.size());
        return ResumeUploadResponse.builder()
                .resumeId(resume.getId())
                .fileName(fileName)
                .status(resume.getStatus())
                .chunkCount(chunks.size())
                .build();
    }

    public List<ResumeListItemResponse> listMine() {
        Long userId = loginUserContext.getCurrentUserId();
        return resumeMapper.selectList(new LambdaQueryWrapper<ResumeEntity>()
                        .eq(ResumeEntity::getUserId, userId)
                        .orderByDesc(ResumeEntity::getCreateTime))
                .stream()
                .map(this::toListItem)
                .toList();
    }

    public ResumeDetailResponse getDetail(Long resumeId) {
        Long userId = loginUserContext.getCurrentUserId();
        ResumeEntity resume = resumeMapper.selectOne(new LambdaQueryWrapper<ResumeEntity>()
                .eq(ResumeEntity::getId, resumeId)
                .eq(ResumeEntity::getUserId, userId)
                .last("limit 1"));
        if (resume == null) {
            throw new BizException(ErrorCode.RESUME_NOT_FOUND);
        }

        List<ResumeChunkResponse> chunks = documentChunkRepository.findByResumeIdAndUserId(resumeId, userId)
                .stream()
                .map(entity -> ResumeChunkResponse.builder()
                        .chunkId(entity.getId())
                        .chunkIndex(entity.getChunkIndex())
                        .chunkText(entity.getChunkText())
                        .metadataJson(entity.getMetadataJson())
                        .embedding(entity.getEmbedding())
                        .embeddingDimensions(entity.getEmbedding().size())
                        .build())
                .toList();

        return ResumeDetailResponse.builder()
                .resumeId(resume.getId())
                .fileName(resume.getFileName())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .status(resume.getStatus())
                .storageUrl(resume.getStorageUrl())
                .rawText(resume.getRawText())
                .structured(readStructured(resume.getStructuredJson()))
                .parseError(resume.getParseError())
                .createTime(resume.getCreateTime())
                .chunks(chunks)
                .build();
    }

    public List<ResumeChunkSearchResponse> searchSimilarChunks(String query, Integer topK) {
        Long userId = loginUserContext.getCurrentUserId();
        if (!StringUtils.hasText(query)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "query is required");
        }

        int limitedTopK = topK == null ? 5 : Math.max(1, Math.min(topK, 20));
        List<Double> queryEmbedding = embeddingService.embed(query.trim());
        return documentChunkRepository.searchByUserId(userId, queryEmbedding, limitedTopK);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "resume file is required");
        }
        if (file.getSize() > resumeProperties.getUpload().getMaxFileSizeBytes()) {
            throw new BizException(ErrorCode.FILE_TOO_LARGE);
        }

        String extension = extensionOf(resolveFileName(file));
        boolean allowed = resumeProperties.getUpload().getAllowedExtensions()
                .stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(extension::equals);
        if (!allowed) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    private String resolveFileName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "file name is missing");
        }
        return fileName.trim();
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private byte[] readContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BizException(ErrorCode.FILE_UPLOAD_FAILED.getCode(), exception.getMessage());
        }
    }

    private String buildObjectKey(Long userId, String extension) {
        return "resumes/" + userId + "/" + UUID.randomUUID() + "." + extension;
    }

    private DocumentChunkEntity toChunkEntity(Long resumeId, Long userId, ResumeTextSplitter.SplitChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("startOffset", chunk.getStartOffset());
        metadata.put("endOffset", chunk.getEndOffset());
        metadata.put("length", chunk.getText().length());

        DocumentChunkEntity entity = new DocumentChunkEntity();
        entity.setResumeId(resumeId);
        entity.setUserId(userId);
        entity.setChunkIndex(chunk.getChunkIndex());
        entity.setChunkText(chunk.getText());
        entity.setMetadataJson(writeJson(metadata));
        entity.setEmbedding(embeddingService.embed(chunk.getText()));
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

    private ResumeListItemResponse toListItem(ResumeEntity entity) {
        return ResumeListItemResponse.builder()
                .resumeId(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

    private StructuredResumeDto readStructured(String structuredJson) {
        if (!StringUtils.hasText(structuredJson)) {
            return new StructuredResumeDto();
        }
        try {
            return objectMapper.readValue(structuredJson, StructuredResumeDto.class);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.RESUME_PARSE_FAILED.getCode(), exception.getMessage());
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.RESUME_PARSE_FAILED.getCode(), exception.getMessage());
        }
    }
}
