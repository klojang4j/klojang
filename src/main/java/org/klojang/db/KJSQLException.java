package org.klojang.db;

import java.sql.SQLException;
import org.klojang.KlojangRTException;
import nl.naturalis.common.ExceptionMethods;

public class KJSQLException extends KlojangRTException {

  static RuntimeException wrap(Throwable t, SQL sql) {
    if (t instanceof KJSQLException) {
      return (KJSQLException) t;
    } else if (t instanceof SQLException) {
      return new KJSQLException("%s ******** %s", t.getMessage(), sql);
    }
    return ExceptionMethods.uncheck(t);
  }

  public KJSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KJSQLException(String message, Throwable cause) {
    super(message, cause);
  }
}
