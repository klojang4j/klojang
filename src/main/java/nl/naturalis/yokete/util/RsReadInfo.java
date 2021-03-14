package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

class RsReadInfo {

  static Map<String, Object> toMap(ResultSet rs, RsReadInfo[] infos) throws Throwable {
    Map<String, Object> map = new HashMap<>(infos.length);
    for (RsReadInfo inf : infos) {
      Object v;
      if (inf.secondArg == null) {
        v = inf.rsMethod.invoke(rs, inf.idx);
      } else {
        v = inf.rsMethod.invoke(rs, inf.idx, inf.secondArg);
      }
      map.put(inf.label, v);
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
  private final Class<?> secondArg;

  RsReadInfo(int idx, String label, int type, MethodHandle mh, Class<?> clazz) {
    this.idx = idx;
    this.label = label;
    this.type = type;
    this.rsMethod = mh;
    this.secondArg = clazz;
  }
}
