package org.klojang.x.acc;

import java.util.Map;
import org.klojang.template.Accessor;
import org.klojang.template.NameMapper;
import org.klojang.template.RenderException;

public class MapAccessor implements Accessor<Map<String, Object>> {

  private final NameMapper nm;

  public MapAccessor() {
    this(null);
  }

  public MapAccessor(NameMapper nm) {
    this.nm = nm;
  }

  @Override
  public Object access(Map<String, Object> data, String name) throws RenderException {
    String key = nm == null ? name : nm.map(name);
    return data.getOrDefault(key, UNDEFINED);
  }
}
