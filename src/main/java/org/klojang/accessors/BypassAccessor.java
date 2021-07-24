package org.klojang.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.RenderException;

/**
 * A dummy {@code Accessor} implementation.
 *
 * @author Ayco Holleman
 */
public class BypassAccessor implements Accessor<Object> {

  public static final BypassAccessor BYPASS_ACCESSOR = new BypassAccessor();

  private static final String ERR = "Illegal reliance on BypassAccessor";

  private BypassAccessor() {}

  /** Throws an {@code UnsupportedOperationException}. */
  @Override
  public Object access(Object sourceData, String varOrNestedTemplateName) throws RenderException {
    throw new UnsupportedOperationException(ERR);
  }
}
