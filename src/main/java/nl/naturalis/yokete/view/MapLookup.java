package nl.naturalis.yokete.view;

import java.util.Map;
import nl.naturalis.common.check.Check;

public class MapLookup implements ViewData {

  private final Map<String, Object> data;

  public MapLookup(Map<String, Object> data) {
    this.data = Check.notNull(data).ok();
  }

  public Object get(String var) {
    return data.getOrDefault(var, ABSENT);
  }
}
