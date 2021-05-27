package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.yokete.db.BindInfo;
import nl.naturalis.yokete.db.NamedParameter;

@ModulePrivate
public class MapBinder {

  private static final Object ABSENT = new Object();

  private List<NamedParameter> params;
  private BindInfo bindInfo;

  public MapBinder(List<NamedParameter> params, BindInfo bindInfo) {
    this.params = params;
    this.bindInfo = bindInfo;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void bindMap(
      PreparedStatement ps, Map<String, Object> map, Collection<NamedParameter> bound)
      throws Throwable {
    ReceiverNegotiator negotiator = ReceiverNegotiator.getInstance();
    for (NamedParameter param : params) {
      String key = param.getName();
      if (!map.containsKey(key)) {
        continue;
      }
      bound.add(param);
      Object v = map.getOrDefault(key, ABSENT);
      if (v == ABSENT) {
        continue;
      } else if (v == null) {
        bind(ps, param, (String) null);
      } else if (Enum.class.isInstance(v) && bindInfo.saveEnumUsingToString(key)) {
        bind(ps, param, ((Enum<?>) v).toString());
      } else {
        Receiver receiver = negotiator.getDefaultReceiver(v.getClass());
        param.getIndices().forEachThrowing(i -> receiver.bind(ps, i, v));
      }
    }
  }

  private static void bind(PreparedStatement ps, NamedParameter param, String val)
      throws SQLException {
    param.getIndices().forEachThrowing(i -> ps.setString(i, val));
  }
}
