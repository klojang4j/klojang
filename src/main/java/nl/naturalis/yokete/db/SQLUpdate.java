package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;

public class SQLUpdate extends SQLStatement {

  private PreparedStatement ps;

  public SQLUpdate(Connection conn, SQL sql) {
    super(conn, sql);
  }

  @Override
  public SQLUpdate bind(Object bean) {
    return (SQLUpdate) super.bind(bean);
  }

  @Override
  public SQLUpdate bind(Map<String, Object> map) {
    return (SQLUpdate) super.bind(map);
  }

  @Override
  public SQLUpdate bind(String param, Object value) {
    return (SQLUpdate) super.bind(param, value);
  }

  public int execute() {
    try {
      ps = con.prepareStatement(sql.getNormalizedSQL());
      bind(ps);
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
