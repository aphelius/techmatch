package com.techmatch.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokenResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private CurrentUserResponse user;
}
