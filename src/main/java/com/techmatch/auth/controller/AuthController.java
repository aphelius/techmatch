package com.techmatch.auth.controller;

import com.techmatch.auth.dto.AuthTokenResponse;
import com.techmatch.auth.dto.CurrentUserResponse;
import com.techmatch.auth.dto.LoginRequest;
import com.techmatch.auth.dto.RegisterRequest;
import com.techmatch.auth.service.AuthService;
import com.techmatch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user and return JWT token")
    public ApiResponse<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with username and password")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Get current login user info", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
