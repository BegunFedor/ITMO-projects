package org.example.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class NameAlreadyExistsException extends BusinessException{
    private static String ERROR_CODE = "NAME_ALREADY_EXISTS";
    private static String ERROR_MESSAGE = "Это имя уже занято";

    public NameAlreadyExistsException(String message){
        super(message, ERROR_CODE, ERROR_MESSAGE);
    }

    public NameAlreadyExistsException withEmail(String email, String name){
        String technicalMessage = String.format("Это имя уже занято: %s", name);
        return (NameAlreadyExistsException) new NameAlreadyExistsException(technicalMessage)
                .withDetail("identifierType", "NAME");
    }
}
