package nl.naturalis.yokete.db;

import java.sql.Connection;

public class SQLQuery extends SQLStatement {

  SQLQuery(Connection conn, SQL sql) {
    super(conn, sql);
  }

  @Override
  public void close() throws Exception {}
}
