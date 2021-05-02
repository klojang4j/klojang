package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import nl.naturalis.yokete.db.BindInfo;
import nl.naturalis.yokete.db.NamedParameter;

public class MapBinder {

  private static final Object ABSENT = new Object();

  private List<NamedParameter> params;
  private BindInfo bindInfo;

  public MapBinder(List<NamedParameter> params, BindInfo bindInfo) {
    this.params = params;
    this.bindInfo = bindInfo;
  }

  public void bindMap(PreparedStatement ps, Map<String, Object> map) throws SQLException {
    for (NamedParameter param : params) {
      if (!map.containsKey(param.getName())) {
        continue;
      }
      Object v = map.getOrDefault(param, ABSENT);
      if (v == ABSENT) {
        continue;
      } else if (v == null) {
        bind(ps, param, (String) null);
      }
    }
  }

  private static void bind(PreparedStatement ps, NamedParameter param, String val)
      throws SQLException {
    for (int idx : param.getIndices()) {
      ps.setString(idx, val);
    }
  }
}
