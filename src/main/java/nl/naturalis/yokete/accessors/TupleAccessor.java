package nl.naturalis.yokete.accessors;

import nl.naturalis.common.Pair;
import nl.naturalis.common.Tuple;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;

/**
 * An {@code Accessor} implementation specialized at accessing {@link Pair} objects. This {@code
 * Accessor} is used by the render session's {@link RenderSession#fillTupleTemplate(String,
 * java.util.List, nl.naturalis.yokete.render.EscapeType) fillTuple} method. Its {@code access}
 * method alternates between handing out the first element and the second element in the {@code
 * Tuple} object.
 *
 * @author Ayco Holleman
 */
public class TupleAccessor implements Accessor<Tuple<?, ?>> {

  private boolean left = true;

  /**
   * Ignores the {@code name} argument and returns either the first or the second value in the
   * {@code Pair}, depending on how often the {@code access} method has been called before.
   */
  @Override
  public Object access(Tuple<?, ?> tuple, String name) throws RenderException {
    Object obj = left ? tuple.getLeft() : tuple.getRight();
    left = !left;
    return obj;
  }
}
