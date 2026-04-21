package prog.lab3.exceptions;

public class UncheckedException extends RuntimeException {
    public UncheckedException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "UncheckedException: " + super.getMessage();
    }
}
