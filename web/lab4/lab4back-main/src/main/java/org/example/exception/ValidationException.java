package org.example.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class ValidationException extends BusinessException{

    private static String ERROR_CODE = "VALIDATION_EXCEPTION";
    private static String ERROR_MESSAGE = "Ошибка валидации";

    public ValidationException(String message){
        super(message, ERROR_CODE, ERROR_MESSAGE);
    }

    public static ValidationException withDescription(String parameter, String message){
        String techMessage = String.format("Ошибка валидации, проверьте %s %s", parameter, message);
        return (ValidationException) new ValidationException(techMessage)
                .withDetail("parameter", parameter)
                .withDetail("message", message);
    }
}
