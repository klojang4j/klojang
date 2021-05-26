package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SQLQuery extends SQLStatement<SQLQuery> {

  private PreparedStatement ps;

  public SQLQuery(Connection con, SQL sql) {
    super(con, sql);
  }

  public Row executeAndMappify() throws Throwable {
    ps = con.prepareStatement(sql.getNormalizedSQL());
    bind(ps);
    try (ResultSet rs = ps.executeQuery()) {
      return null;
    }
  }

  @Override
  public void close() throws Exception {
    close(ps);
  }
}
