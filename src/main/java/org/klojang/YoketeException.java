package org.klojang;

/**
 * Base class for checked exceptions emanating from the Yokete package.
 *
 * @author Ayco Holleman
 */
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
