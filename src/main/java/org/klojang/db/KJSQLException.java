package org.klojang.db;

import java.sql.SQLException;
import org.klojang.KlojangRTException;

public class KJSQLException extends KlojangRTException {

  public KJSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KJSQLException(Throwable cause) {
    super(cause);
  }

  public KJSQLException(SQL sql, SQLException e) {
    super(e.getMessage() + "\n" + sql.getJdbcSQL());
  }
}
