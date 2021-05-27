package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import nl.naturalis.common.ExceptionMethods;

public class SQLUpdate extends SQLStatement<SQLUpdate> {

  private PreparedStatement ps;

  public SQLUpdate(Connection conn, SQL sql) {
    super(conn, sql);
  }

  public int execute() {
    try {
      ps = con.prepareStatement(sql.getNormalizedSQL());
      applyBindings(ps);
      return ps.executeUpdate();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  public void close() {
    close(ps);
  }
}
