package org.klojang.x.accessors;

import java.util.Map;
import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;

public class MapAccessor implements Accessor<Map<String, Object>> {

  private final NameMapper nm;

  public MapAccessor(NameMapper nm) {
    this.nm = nm;
  }

  @Override
  public Object access(Map<String, Object> data, String name) throws RenderException {
    return data.getOrDefault(nm.map(name), UNDEFINED);
  }
}
