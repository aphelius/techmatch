package com.techmatch.match.controller;

import com.techmatch.common.response.ApiResponse;
import com.techmatch.match.dto.CreateMatchRequest;
import com.techmatch.match.dto.CreateMatchResponse;
import com.techmatch.match.dto.MatchReportResponse;
import com.techmatch.match.service.MatchService;
import com.techmatch.task.dto.AgentTaskResponse;
import com.techmatch.task.dto.AgentTimelineEventResponse;
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
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Match task and timeline APIs")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    @Operation(summary = "Create match task", description = "Create a resume-job match task and initialize agent timeline")
    public ApiResponse<CreateMatchResponse> create(@RequestBody @Valid CreateMatchRequest request) {
        return ApiResponse.success(matchService.createMatchTask(request));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get match task", description = "Query current user's task status and current node")
    public ApiResponse<AgentTaskResponse> getTask(@PathVariable Long taskId) {
        return ApiResponse.success(matchService.getTask(taskId));
    }

    @GetMapping("/{taskId}/timeline")
    @Operation(summary = "Get task timeline", description = "Query current user's agent execution timeline")
    public ApiResponse<List<AgentTimelineEventResponse>> getTimeline(@PathVariable Long taskId) {
        return ApiResponse.success(matchService.getTimeline(taskId));
    }

    @GetMapping("/{taskId}/report")
    @Operation(summary = "Get match report", description = "Query current user's final match report")
    public ApiResponse<MatchReportResponse> getReport(@PathVariable Long taskId) {
        return ApiResponse.success(matchService.getReport(taskId));
    }
}
