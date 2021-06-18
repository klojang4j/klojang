package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
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
      Object input = map.getOrDefault(key, ABSENT);
      if (input == ABSENT) {
        continue;
      } else if (input == null) {
        param.getIndices().forEachThrowing(i -> ps.setString(i, null));
      } else {
        Receiver receiver;
        if (Enum.class.isInstance(input) && bindInfo.saveEnumUsingToString(key)) {
          receiver = EnumReceivers.ENUM_TO_STRING;
        } else {
          receiver = negotiator.getDefaultReceiver(input.getClass());
        }
        Object output = receiver.getParamValue(input);
        param.getIndices().forEachThrowing(i -> receiver.bind(ps, i, output));
      }
    }
  }
}
