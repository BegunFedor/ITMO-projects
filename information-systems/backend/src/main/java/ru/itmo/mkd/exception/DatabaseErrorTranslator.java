package ru.itmo.mkd.exception;

import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseErrorTranslator {

    private static final String INSUFFICIENT_PRIVILEGE = "42501";
    private static final String NO_DATA_FOUND = "P0002";
    private static final String UNIQUE_VIOLATION = "23505";
    private static final String SERIALIZATION_FAILURE = "40001";
    private static final String DEADLOCK_DETECTED = "40P01";

    public RuntimeException translate(DataAccessException exception) {
        PSQLException psql = findPsqlException(exception);
        if (psql == null) {
            return exception;
        }
        ServerErrorMessage server = psql.getServerErrorMessage();
        String sqlState = psql.getSQLState();
        String hint = server != null ? server.getHint() : null;
        String message = server != null ? server.getMessage() : psql.getMessage();

        if (INSUFFICIENT_PRIVILEGE.equals(sqlState)) {
            return new ForbiddenException(codeOr(hint, ErrorCodes.FORBIDDEN), message, exception);
        }
        if (NO_DATA_FOUND.equals(sqlState)) {
            return ErrorCodes.NOT_FOUND.equals(hint)
                    ? new NotFoundException(hint, message, exception)
                    : new ConflictException(codeOr(hint, ErrorCodes.CONFLICT), message, exception);
        }
        if (UNIQUE_VIOLATION.equals(sqlState)) {
            return new ConflictException(codeOr(hint, constraintCode(server)), message, exception);
        }
        if (SERIALIZATION_FAILURE.equals(sqlState) || DEADLOCK_DETECTED.equals(sqlState)) {
            return new ConflictException(ErrorCodes.CONFLICT, message, exception);
        }
        if (hint != null || (sqlState != null && sqlState.startsWith("23"))) {
            return new BusinessRuleException(codeOr(hint, constraintCode(server)), message, exception);
        }
        return exception;
    }

    private static PSQLException findPsqlException(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof PSQLException psql) {
                return psql;
            }
        }
        return null;
    }

    private static String codeOr(String hint, String fallback) {
        return hint != null && !hint.isBlank() ? hint : fallback;
    }

    private static String constraintCode(ServerErrorMessage server) {
        String constraint = server != null ? server.getConstraint() : null;
        return constraint != null ? "CONSTRAINT:" + constraint : ErrorCodes.BUSINESS_RULE;
    }
}
