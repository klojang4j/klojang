package org.klojang.db;

import java.sql.SQLException;
import org.klojang.KlojangRTException;
import nl.naturalis.common.ExceptionMethods;

public class KJSQLException extends KlojangRTException {

  static RuntimeException wrap(Throwable t, SQL sql) {
    return ExceptionMethods.wrap(t, KJSQLException::new, "Error while executing SQL:\n%s", sql);
  }

  static KJSQLException wrap(SQLException e, SQL sql) {
    return new KJSQLException("%s\n%s", sql, e.getMessage());
  }

  public KJSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KJSQLException(Throwable cause) {
    super(cause);
  }

  public KJSQLException(String message, Throwable cause) {
    super(message, cause);
  }

  public KJSQLException(SQL sql, Throwable cause) {
    super(sql + "\n" + cause.getMessage());
  }
}
