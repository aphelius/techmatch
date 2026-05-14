package com.techmatch.auth.security;

import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoginUserContext {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserPrincipal principal)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
