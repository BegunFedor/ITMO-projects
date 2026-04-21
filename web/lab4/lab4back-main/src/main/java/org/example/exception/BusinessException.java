package org.example.exception;

import java.util.HashMap;
import java.util.Map;

public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;
    private final Map<String, Object> details;


    public BusinessException(String message, String errorCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.details = new HashMap<>();
    }

    public String getErrorCode() { return errorCode; }
    public String getUserMessage() { return userMessage; }
    public Map<String, Object> getDetails() { return details; }

    public BusinessException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}