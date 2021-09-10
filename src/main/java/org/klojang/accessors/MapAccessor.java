package org.klojang.accessors;

import java.util.Map;
import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;

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
