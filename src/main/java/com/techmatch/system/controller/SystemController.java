package com.techmatch.system.controller;

import com.techmatch.common.response.ApiResponse;
import com.techmatch.system.dto.InfraStatusResponse;
import com.techmatch.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Tag(name = "System", description = "System and health APIs")
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "Simple backend ping for frontend connectivity check")
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.success(Map.of("status", "ok", "service", "techmatch"));
    }

    @GetMapping("/infra")
    @Operation(summary = "Infrastructure status", description = "Check backend dependencies")
    public ApiResponse<InfraStatusResponse> infra() {
        return ApiResponse.success(systemService.checkInfrastructure());
    }
}
