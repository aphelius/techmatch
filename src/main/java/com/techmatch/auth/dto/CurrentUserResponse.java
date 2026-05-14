package com.techmatch.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentUserResponse {

    private Long userId;
    private String username;
    private String nickname;
}
