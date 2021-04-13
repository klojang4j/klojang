package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
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
  private final MethodHandle rsMethod;
  /* Class object passed as 2nd arg to ResultSet.getObject */
  private final Class<?> classArg;

  ColumnReader(int idx, String label, int type, MethodHandle mh, Class<?> classArg) {
    this.idx = idx;
    this.label = label;
    this.type = type;
    this.rsMethod = mh;
    this.classArg = classArg;
  }

  Object readColumn(ResultSet rs) throws Throwable {
    if (classArg == null) {
      return rsMethod.invoke(rs, idx);
    }
    return rsMethod.invoke(rs, idx, classArg);
  }
}
