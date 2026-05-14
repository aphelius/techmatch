package com.techmatch.resume.controller;

import com.techmatch.common.response.ApiResponse;
import com.techmatch.resume.dto.ResumeDetailResponse;
import com.techmatch.resume.dto.ResumeChunkSearchResponse;
import com.techmatch.resume.dto.ResumeListItemResponse;
import com.techmatch.resume.dto.ResumeUploadResponse;
import com.techmatch.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "Resume upload and query APIs")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload resume",
            description = "Upload PDF or DOCX resume, persist file, extract text, generate structure and chunks",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object")
            ))
    )
    public ApiResponse<ResumeUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(resumeService.upload(file));
    }

    @GetMapping
    @Operation(summary = "List resumes", description = "List current user's resumes")
    public ApiResponse<List<ResumeListItemResponse>> listMine() {
        return ApiResponse.success(resumeService.listMine());
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get resume detail", description = "Get parsed resume detail and chunks")
    public ApiResponse<ResumeDetailResponse> detail(@PathVariable Long resumeId) {
        return ApiResponse.success(resumeService.getDetail(resumeId));
    }

    @GetMapping("/search/chunks")
    @Operation(summary = "Search similar resume chunks", description = "Retrieve similar chunks with pgvector cosine similarity")
    public ApiResponse<List<ResumeChunkSearchResponse>> searchChunks(@RequestParam String query,
                                                                     @RequestParam(required = false) Integer topK) {
        return ApiResponse.success(resumeService.searchSimilarChunks(query, topK));
    }
}
