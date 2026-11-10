package ru.itmo.mkd.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {

    public AuthenticationException(String code, String message) {
        super(code, message);
    }

    public AuthenticationException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static AuthenticationException of(String message) {
        return new AuthenticationException(ErrorCodes.UNAUTHENTICATED, message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
