package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;

public class ResultSetMappifier {

  public ResultSetMappifier() {}

  public Map<String, Object> mappify(ResultSet rs) {
    try {
      return mappifyOne(rs);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  public List<Map<String, Object>> mappify(ResultSet rs, int limit) {
    List<Map<String, Object>> all = new ArrayList<>(limit);
    try {
      for (int i = 0; rs.next() && i < limit; i++) {
        all.add(mappifyOne(rs));
      }
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return all;
  }

  public List<Map<String, Object>> mappifyAll(ResultSet rs) {
    List<Map<String, Object>> all = new ArrayList<>();
    try {
      while (rs.next()) {
        all.add(mappifyOne(rs));
      }
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return all;
  }

  private static Map<String, Object> mappifyOne(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int sz = rsmd.getColumnCount();
    Map<String, Object> map = new HashMap<>(sz);
    for (int i = 0; i < sz; i++) {
      map.put(rsmd.getColumnLabel(i + 1), rs.getObject(i + 1));
    }
    return map;
  }
}
