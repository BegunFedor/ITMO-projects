package prog.lab3.exceptions;

public class IllegalActionException extends Exception {
  public IllegalActionException(String message) {
    super(message);
  }

  @Override
  public String getMessage() {
    return "Недопустимое действие: " + super.getMessage();
  }
}
