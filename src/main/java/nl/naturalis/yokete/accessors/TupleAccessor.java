package nl.naturalis.yokete.accessors;

import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;
import nl.naturalis.yokete.template.Template;

/**
 * An {@code Accessor} implementation specialized at accessing {@link Tuple} objects. This {@code
 * Accessor} is used by the render session's {@link RenderSession#populateTuple(String,
 * java.util.List, nl.naturalis.yokete.render.EscapeType) fillTuple} method.
 *
 * @author Ayco Holleman
 */
public class TupleAccessor implements Accessor<Tuple<?, ?>> {

  private final String[] vars;

  public TupleAccessor(Template template) {
    this.vars = Check.notNull(template).ok().getVars().toArray(String[]::new);
  }

  @Override
  public Object access(Tuple<?, ?> tuple, String name) throws RenderException {
    return name.equals(vars[0])
        ? tuple.getLeft()
        : name.equals(vars[1]) ? tuple.getRight() : UNDEFINED;
  }
}
