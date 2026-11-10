package ru.itmo.mkd.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String code, String message) {
        super(code, message);
    }

    public ConflictException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static ConflictException of(String message) {
        return new ConflictException(ErrorCodes.CONFLICT, message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
