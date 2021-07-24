package org.klojang.render;

import java.util.Map;

/**
 * Same as MapAccessor but without the checks and niceties.
 *
 * @author Ayco Holleman
 */
class MapAccessorInternal implements Accessor<Map<String, Object>> {

  @Override
  public Object access(Map<String, Object> from, String name) throws RenderException {
    return from.get(name);
  }
}
