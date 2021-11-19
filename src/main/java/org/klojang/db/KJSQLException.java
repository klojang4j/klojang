package org.klojang.db;

import java.sql.SQLException;
import org.klojang.KlojangRTException;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.exception.UncheckedException;

public class KJSQLException extends KlojangRTException {

  public static RuntimeException wrap(Throwable exc, SQL sql) {
    Check.notNull(exc);
    Check.notNull(sql);
    if (exc instanceof KJSQLException) {
      return (KJSQLException) exc;
    } else if (exc instanceof SQLException) {
      return new KJSQLException(sql, (SQLException) exc);
    } else if (exc.getClass() == UncheckedException.class) {
      // Make sure we can thoughtlessly wrap any exception we encounter
      return wrap(((UncheckedException) exc).unwrap(), sql);
    }
    return ExceptionMethods.uncheck(exc);
  }

  public KJSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KJSQLException(String message, SQLException cause) {
    super(message, cause);
  }

  public KJSQLException(SQLException cause) {
    super(cause);
  }

  public KJSQLException(SQL sql, SQLException cause) {
    super(message(sql, cause), cause);
  }

  private static String message(SQL sql, SQLException cause) {
    return cause.getMessage() + " >>>> while executing: " + sql;
  }
}
