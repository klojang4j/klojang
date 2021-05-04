package nl.naturalis.yokete.db;

import nl.naturalis.yokete.YoketeRuntimeException;

public class KSQLException extends YoketeRuntimeException {

  public KSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KSQLException(Throwable cause) {
    super(cause);
  }

  public KSQLException(String message, Throwable cause) {
    super(message, cause);
  }
}
