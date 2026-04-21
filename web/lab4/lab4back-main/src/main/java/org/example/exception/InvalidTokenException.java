package org.example.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class InvalidTokenException extends BusinessException{
    private static String ERROR_CODE = "INVALID_TOKEN";
    private static String ERROR_MESSAGE = "Токен просрочен";

    public InvalidTokenException(String message){
        super(message, ERROR_CODE, ERROR_MESSAGE);
    }
}
