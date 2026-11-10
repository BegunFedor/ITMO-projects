package ru.itmo.mkd.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String code, String message) {
        super(code, message);
    }

    public ForbiddenException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static ForbiddenException of(String message) {
        return new ForbiddenException(ErrorCodes.FORBIDDEN, message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
