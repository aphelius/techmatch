package com.techmatch.common.response;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public final class RequestIdHolder {

    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    private RequestIdHolder() {
    }

    public static String getRequestId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object value = attributes.getAttribute(REQUEST_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return value instanceof String requestId ? requestId : null;
    }
}
