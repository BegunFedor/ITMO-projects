package ru.itmo.mkd.dto;

import java.util.List;

public record ErrorResponse(String code, List<FieldViolation> fields) {

    public static ErrorResponse of(String code) {
        return new ErrorResponse(code, List.of());
    }
}
