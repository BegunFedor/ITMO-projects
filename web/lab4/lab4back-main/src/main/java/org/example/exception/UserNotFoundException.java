package org.example.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class UserNotFoundException extends BusinessException{


    private static final String ERROR_CODE="USER_NOT_FOUND";
    private static final String ERROR_MESSAGE="Пользователь не найден";

    public UserNotFoundException(String message){
        super(message, ERROR_CODE, ERROR_MESSAGE);
    }

    public static UserNotFoundException byEmail(String email) {
        String technicalMessage = String.format("User not found with email: %s", email);
        return (UserNotFoundException) new UserNotFoundException(technicalMessage)
                .withDetail("identifier", email)
                .withDetail("identifierType", "EMAIL");
    }
}
