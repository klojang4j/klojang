package org.klojang.x.acc;

import org.klojang.template.Accessor;
import org.klojang.template.RenderException;

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
