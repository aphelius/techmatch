package com.techmatch.common.response;

import com.techmatch.common.enums.ErrorCode;
import lombok.Data;

@Data
public class ApiResponse<T> {

    private Boolean success;
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(ErrorCode.SUCCESS.getCode());
        response.setMessage(ErrorCode.SUCCESS.getMessage());
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        response.setRequestId(RequestIdHolder.getRequestId());
        return response;
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        response.setRequestId(RequestIdHolder.getRequestId());
        return response;
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }
}
