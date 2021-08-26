package org.klojang.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.RenderException;

/**
 * An {@link Accessor} implementation that always returns {@link Accessor#UNDEFINED}.
 *
 * @author Ayco Holleman
 */
public class NullAccessor implements Accessor<Object> {

  public NullAccessor() {}

  @Override
  public Object access(Object data, String property) throws RenderException {
    return UNDEFINED;
  }
}
