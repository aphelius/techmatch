package com.techmatch.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "request params invalid"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "resource not found"),
    METHOD_NOT_ALLOWED(405, "method not allowed"),
    INTERNAL_ERROR(500, "internal server error"),
    INFRASTRUCTURE_ERROR(10001, "infrastructure unavailable"),
    USER_ALREADY_EXISTS(10002, "username already exists"),
    USERNAME_OR_PASSWORD_INVALID(10003, "username or password is invalid"),
    TOKEN_INVALID(10004, "token is invalid"),
    FILE_TYPE_NOT_SUPPORTED(10005, "file type is not supported"),
    FILE_TOO_LARGE(10006, "file is too large"),
    FILE_UPLOAD_FAILED(10007, "file upload failed"),
    RESUME_NOT_FOUND(10008, "resume not found"),
    RESUME_PARSE_FAILED(10009, "resume parse failed"),
    JOB_NOT_FOUND(10010, "job description not found"),
    AGENT_TASK_NOT_FOUND(10011, "agent task not found"),
    MATCH_REPORT_NOT_FOUND(10012, "match report not found"),
    INTERVIEW_QUESTION_NOT_FOUND(10013, "interview question not found");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
