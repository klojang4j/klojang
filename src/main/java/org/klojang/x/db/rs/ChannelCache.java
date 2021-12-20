package org.klojang.x.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.klojang.db.SQLTypeNames;
import org.klojang.template.NameMapper;

@SuppressWarnings("rawtypes")
public class ChannelCache {

  static final ChannelCache INSTANCE = new ChannelCache();

  /**
   * Verifies that the specified {@code Resultset} can be beanified/mappified by the specified
   * transporters. This is the case if the {@code Resultset} has a column count equal to the length
   * of the {@code transporters} array, and if the sql types of the columns pair up with the sql
   * types of the transporters. Column names/labels are ignored.
   *
   * @param rs
   * @param transporters
   * @return
   * @throws SQLException
   */
  public static boolean isCompatible(ResultSet rs, Channel[] transporters) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    if (rsmd.getColumnCount() != transporters.length) {
      return false;
    }
    for (int i = 0; i < transporters.length; ++i) {
      if (transporters[i].getSqlType() != rsmd.getColumnType(i + 1)) {
        return false;
      }
    }
    return true;
  }

  public static List<String> getMatchErrors(ResultSet rs, Channel[] transporters)
      throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    List<String> errors = new ArrayList<>();
    if (rsmd.getColumnCount() != transporters.length) {
      String fmt = "Expected column count: %d. Actual column count: %d";
      String msg = String.format(fmt, transporters.length, rsmd.getColumnCount());
      errors.add(msg);
    }
    int min = Math.min(transporters.length, rsmd.getColumnCount());
    for (int i = 0; i < min; ++i) {
      if (transporters[i].getSqlType() != rsmd.getColumnType(i + 1)) {
        String expected = SQLTypeNames.getTypeName(transporters[i].getSqlType());
        String actual = SQLTypeNames.getTypeName(rsmd.getColumnType(i + 1));
        String fmt = "Colum %3d: expected type: %s; actual type: %s";
        String msg = String.format(fmt, i + 1, expected, actual);
        errors.add(msg);
      }
    }
    return errors;
  }

  private final HashMap<RsStrongIdentifier, BeanChannel[]> beanChannels = new HashMap<>();
  private final HashMap<RsStrongIdentifier, RowChannel[]> rowChannels = new HashMap<>();

  private ChannelCache() {}

  public BeanChannel[] getBeanChannels(ResultSet rs, Class clazz, NameMapper mapper) {
    RsStrongIdentifier id = new RsStrongIdentifier(rs);
    return beanChannels.computeIfAbsent(
        id, k -> BeanChannel.createChannels(rs, clazz, mapper));
  }

  public RowChannel[] getRowChannels(ResultSet rs, NameMapper mapper) {
    RsStrongIdentifier id = new RsStrongIdentifier(rs);
    return rowChannels.computeIfAbsent(id, k -> RowChannel.createChannels(rs, mapper));
  }
}
