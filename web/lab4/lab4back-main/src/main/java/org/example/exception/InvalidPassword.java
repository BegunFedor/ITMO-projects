package org.example.exception;


import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class InvalidPassword extends BusinessException {
    private static String ERROR_CODE="INVALID_PASSWORD";
    private static String ERROR_MESSAGE="Неверный пароль";

    public InvalidPassword(String message) {
        super(message, ERROR_CODE, ERROR_MESSAGE);
    }

    public static InvalidPassword withEmail(String email){
        String technicalMessage = String.format("Пароль неверный для email: %s", email);
        return (InvalidPassword) new InvalidPassword(technicalMessage)
                .withDetail("identifier", email)
                .withDetail("identifierType", "EMAIL");

    }
}
