package nl.naturalis.yokete;

/**
 * Base class for runtime exceptions emanating from the Yokete package.
 *
 * @author Ayco Holleman
 */
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
