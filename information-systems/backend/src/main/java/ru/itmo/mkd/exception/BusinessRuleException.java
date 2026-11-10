package ru.itmo.mkd.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String code, String message) {
        super(code, message);
    }

    public BusinessRuleException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static BusinessRuleException of(String message) {
        return new BusinessRuleException(ErrorCodes.BUSINESS_RULE, message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
