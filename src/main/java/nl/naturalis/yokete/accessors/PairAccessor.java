package nl.naturalis.yokete.accessors;

import nl.naturalis.common.Pair;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;
import nl.naturalis.yokete.template.Template;

/**
 * An {@code Accessor} implementation specialized at accessing {@link Pair} objects. This {@code
 * Accessor} is used by the render session's {@link RenderSession#fillPair(String, java.util.List,
 * nl.naturalis.yokete.render.EscapeType) fillTuple} method. Its {@code access} method alternates
 * between handing out the first value and the second value in the {@code Pair} object.
 *
 * @author Ayco Holleman
 */
public class PairAccessor implements Accessor<Pair<Object>> {

  private boolean first = true;

  /**
   * Ignores the {@code name} argument and returns either the first or the second value in the
   * {@code Pair}, depending on how often the {@code access} method has been called before.
   */
  @Override
  public Object access(Pair<Object> pair, String name) throws RenderException {
    Object obj = first ? pair.getFirst() : pair.getSecond();
    first = !first;
    return obj;
  }

  /** Throws an {@link UnsupportedOperationException}. */
  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    throw new UnsupportedOperationException();
  }
}
