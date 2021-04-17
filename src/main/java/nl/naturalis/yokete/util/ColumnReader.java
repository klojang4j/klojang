package nl.naturalis.yokete.util;

import java.sql.ResultSet;

class ColumnReader {

  static Row toMap(ResultSet rs, ColumnReader[] infos, int mapSize) throws Throwable {
    Row map = new Row(mapSize);
    for (ColumnReader inf : infos) {
      map.put(inf.label, inf.readColumn(rs));
    }
    return map;
  }

  /* one of the constants in java.sql.Types */
  final int type;

  /* one-based column index in the SELECT clause */
  private final int idx;

  /* column name or column alias */
  private final String label;

  /* one of the getXXX methods in ResultSet */
  private final ResultSetGetter getter;

  ColumnReader(int idx, String label, int type, ResultSetGetter getter) {
    this.idx = idx;
    this.label = label;
    this.type = type;
    this.getter = getter;
  }

  Object readColumn(ResultSet rs) throws Throwable {
    if (getter.getClassArgument() == null) {
      return getter.getMethod().invoke(rs, idx);
    }
    return getter.getMethod().invoke(rs, idx, getter.getClassArgument());
  }
}
