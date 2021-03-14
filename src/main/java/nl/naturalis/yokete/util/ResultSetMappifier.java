package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;

public class ResultSetMappifier {

  private final RsReadInfo[] infos;

  ResultSetMappifier(RsReadInfo[] infos) {
    this.infos = infos;
  }

  /**
   * The {@code mappify} methods do not verify whether they can actually mappify the {@code
   * ResultSet} instance that is passed to them. They assume that it has the same metadata (column
   * count and column types) as the {@code ResultSet} that was used to create the mappifier.
   * Strictly speaking, though, they don't know that. If they get passed a {@code ResultSet} with a
   * different layout, all sorts of errors might ensue, including an {@link
   * ArrayIndexOutOfBoundsException}. If you want to be absolutely sure that won't happen, you can
   * call this method first.
   *
   * @param rs
   * @return
   * @throws SQLException
   */
  public boolean canMappify(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    if (rsmd.getColumnCount() != infos.length) {
      return false;
    }
    for (int i = 0; i < infos.length; ++i) {
      if (infos[i].type != rsmd.getColumnType(i + 1)) {
        return false;
      }
    }
    return true;
  }

  public Map<String, Object> mappify(ResultSet rs) {
    Check.notNull(rs);
    try {
      return RsReadInfo.toMap(rs, infos);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Map<String, Object>> mappify(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    List<Map<String, Object>> all = new ArrayList<>(limit);
    try {
      for (int i = 0; rs.next() && i < limit; ++i) {
        all.add(RsReadInfo.toMap(rs, infos));
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
        all.add(RsReadInfo.toMap(rs, infos));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }
}
