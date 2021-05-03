package nl.naturalis.yokete.db;

import java.sql.Connection;

public class SQLUpdate extends SQLStatement {

  public SQLUpdate(Connection conn, SQL sql) {
    super(conn, sql);
  }

  @Override
  public void close() throws Exception {}
}
