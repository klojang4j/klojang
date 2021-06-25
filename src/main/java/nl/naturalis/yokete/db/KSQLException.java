package nl.naturalis.yokete.db;

import java.sql.SQLException;
import nl.naturalis.yokete.YoketeRuntimeException;

public class KSQLException extends YoketeRuntimeException {

  public KSQLException(String message, Object... msgArgs) {
    super(message, msgArgs);
  }

  public KSQLException(Throwable cause) {
    super(cause);
  }

  public KSQLException(SQL sql, SQLException e) {
    super(e.toString() + "  *****  " + sql.getJdbcSQL());
  }
}
