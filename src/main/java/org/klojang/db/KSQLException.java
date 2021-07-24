package org.klojang.db;

import java.sql.SQLException;
import org.klojang.YoketeRuntimeException;

public class KSQLException extends YoketeRuntimeException {

  public KSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KSQLException(Throwable cause) {
    super(cause);
  }

  public KSQLException(SQL sql, SQLException e) {
    super(e.getMessage() + "\n" + sql.getJdbcSQL());
  }
}
