package com.techmatch.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techmatch.auth.dto.AuthTokenResponse;
import com.techmatch.auth.dto.CurrentUserResponse;
import com.techmatch.auth.dto.LoginRequest;
import com.techmatch.auth.dto.RegisterRequest;
import com.techmatch.auth.entity.AppUserEntity;
import com.techmatch.auth.mapper.UserMapper;
import com.techmatch.auth.security.JwtService;
import com.techmatch.auth.security.LoginUserContext;
import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginUserContext loginUserContext;

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        if (findByUsername(username) != null) {
            throw new BizException(ErrorCode.USER_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        AppUserEntity entity = new AppUserEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setNickname(resolveNickname(request.getNickname(), username));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        userMapper.insert(entity);

        return buildTokenResponse(entity);
    }

    public AuthTokenResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        AppUserEntity user = findByUsername(username);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_INVALID);
        }
        return buildTokenResponse(user);
    }

    public CurrentUserResponse currentUser() {
        Long userId = loginUserContext.getCurrentUserId();
        AppUserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    private AppUserEntity findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<AppUserEntity>()
                .eq(AppUserEntity::getUsername, username)
                .last("limit 1"));
    }

    private AuthTokenResponse buildTokenResponse(AppUserEntity user) {
        return AuthTokenResponse.builder()
                .accessToken(jwtService.generateToken(user.getId(), user.getUsername()))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpireSeconds())
                .user(toCurrentUserResponse(user))
                .build();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String resolveNickname(String nickname, String username) {
        String normalizedNickname = normalize(nickname);
        return StringUtils.hasText(normalizedNickname) ? normalizedNickname : username;
    }

    private CurrentUserResponse toCurrentUserResponse(AppUserEntity user) {
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
