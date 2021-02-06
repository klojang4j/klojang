package nl.naturalis.yokete.util;

import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.view.ViewData;

public class MapViewData implements ViewData {

  private Map<String, Object> data;

  public void setData(Map<String, Object> data) {
    this.data = Check.notNull(data).ok();
  }

  public Object getVariableValue(String key) {
    return data.getOrDefault(key, ABSENT);
  }
}
