package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

public class ResultSetMappifier {

  private final Map<String, Tuple<MethodHandle, Class<?>>> invokers;

  ResultSetMappifier(Map<String, Tuple<MethodHandle, Class<?>>> invokers) {
    this.invokers = invokers;
  }

  public Map<String, Object> mappify(ResultSet rs) {
    Check.notNull(rs);
    try {
      return mappifyOne(rs);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Map<String, Object>> mappify(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    List<Map<String, Object>> all = new ArrayList<>(limit);
    try {
      for (int i = 0; rs.next() && i < limit; i++) {
        all.add(mappifyOne(rs));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  public List<Map<String, Object>> mappifyAll(ResultSet rs, int expectedSize) {
    Check.notNull(rs, "rs");
    Check.that(expectedSize, "expectedSize").is(gt(), 0);
    List<Map<String, Object>> all = new ArrayList<>(expectedSize);
    try {
      while (rs.next()) {
        all.add(mappifyOne(rs));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  private Map<String, Object> mappifyOne(ResultSet rs) throws Throwable {
    Map<String, Object> map = new HashMap<>(invokers.size());
    for (Map.Entry<String, Tuple<MethodHandle, Class<?>>> e : invokers.entrySet()) {
      String columnLabel = e.getKey();
      MethodHandle mh = e.getValue().getLeft();
      Class<?> clazz = e.getValue().getRight();
      Object value;
      if (clazz == null) {
        value = mh.invoke(rs, columnLabel);
      } else {
        value = mh.invoke(rs, columnLabel, clazz);
      }
      map.put(columnLabel, value);
    }
    return map;
  }
}
