package exceptions;


public class RecoursiveCallException extends RuntimeException {
    public RecoursiveCallException(String message) {
        super(message);
    }
}