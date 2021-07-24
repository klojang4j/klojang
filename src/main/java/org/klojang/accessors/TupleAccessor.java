package org.klojang.accessors;

import org.klojang.render.Accessor;
import org.klojang.render.RenderException;
import org.klojang.render.RenderSession;
import org.klojang.template.Template;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;

/**
 * An {@code Accessor} implementation specialized at accessing {@link Tuple} objects. This {@code
 * Accessor} is used by the render session's {@link RenderSession#populateWithTuple(String,
 * java.util.List, org.klojang.render.EscapeType) populateWithTuple} method.
 *
 * @author Ayco Holleman
 */
public class TupleAccessor implements Accessor<Tuple<?, ?>> {

  private final String[] vars;

  public TupleAccessor(Template template) {
    this.vars = Check.notNull(template).ok().getVariables().toArray(new String[2]);
  }

  @Override
  public Object access(Tuple<?, ?> tuple, String name) throws RenderException {
    return name.equals(vars[0])
        ? tuple.getLeft()
        : name.equals(vars[1]) ? tuple.getRight() : UNDEFINED;
  }
}
