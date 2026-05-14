package com.techmatch.common.exception;

import com.techmatch.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
