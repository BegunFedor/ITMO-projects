package ru.itmo.mkd.exception;

public final class ErrorCodes {

    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String UNAUTHENTICATED = "UNAUTHENTICATED";
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String CONFLICT = "CONFLICT";
    public static final String BUSINESS_RULE = "BUSINESS_RULE";
    public static final String CSRF_HEADER_MISSING = "CSRF_HEADER_MISSING";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCodes() {
    }
}
