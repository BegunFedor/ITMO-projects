package prog.lab3.exceptions;

public class MissingItemException extends RuntimeException {
    public MissingItemException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Предмет отсутствует: " + super.getMessage();
    }
}
