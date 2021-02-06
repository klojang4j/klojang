package nl.naturalis.yokete.util;

import java.util.Map;
import nl.naturalis.common.check.Check;

public class MapViewData extends AbstractViewData {

  private Map<String, Object> data;

  public MapViewData(ViewDataStringifiers stringifiers) {
    super(stringifiers);
  }

  public void setData(Map<String, Object> data) {
    this.data = Check.notNull(data).ok();
  }

  @Override
  protected Object getRawValue(String var) {
    return Check.notNull(data).ok().getOrDefault(var, ABSENT);
  }
}
