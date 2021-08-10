package org.klojang.x.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.RenderException;

public class NullAccessor implements Accessor<Object> {

  public NullAccessor() {}

  @Override
  public Object access(Object data, String property) throws RenderException {
    return UNDEFINED;
  }
}
