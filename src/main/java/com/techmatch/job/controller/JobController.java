package com.techmatch.job.controller;

import com.techmatch.common.response.ApiResponse;
import com.techmatch.job.dto.CreateJobRequest;
import com.techmatch.job.dto.JobCreateResponse;
import com.techmatch.job.dto.JobDetailResponse;
import com.techmatch.job.dto.JobListItemResponse;
import com.techmatch.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "Job description creation and query APIs")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Create job description", description = "Create a job description, structure it and persist chunks")
    public ApiResponse<JobCreateResponse> create(@RequestBody @Valid CreateJobRequest request) {
        return ApiResponse.success(jobService.create(request));
    }

    @GetMapping
    @Operation(summary = "List job descriptions", description = "List current user's job descriptions")
    public ApiResponse<List<JobListItemResponse>> listMine() {
        return ApiResponse.success(jobService.listMine());
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job detail", description = "Get job detail, structured fields and chunks")
    public ApiResponse<JobDetailResponse> detail(@PathVariable Long jobId) {
        return ApiResponse.success(jobService.getDetail(jobId));
    }
}
