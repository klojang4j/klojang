package nl.naturalis.yokete;

public class YoketeException extends Exception {

  public YoketeException(String message) {
    super(message);
  }

  public YoketeException(Throwable cause) {
    super(cause);
  }

  public YoketeException(String message, Throwable cause) {
    super(message, cause);
  }
}
