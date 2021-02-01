package nl.naturalis.yokete;

public class YoketeRuntimeException extends RuntimeException {

  public YoketeRuntimeException(String message) {
    super(message);
  }

  public YoketeRuntimeException(Throwable cause) {
    super(cause);
  }

  public YoketeRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
