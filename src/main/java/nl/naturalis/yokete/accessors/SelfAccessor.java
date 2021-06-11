package nl.naturalis.yokete.accessors;

import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.EscapeType;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;

/**
 * An {@code Accessor} implementation that returns the source data itself rather than any particular
 * value within it. Used by the render session's {@link RenderSession#populateMonoTemplate(String,
 * Object, EscapeType) fillMono} method.
 *
 * @author Ayco Holleman
 */
public class SelfAccessor implements Accessor<Object> {

  public SelfAccessor() {}

  /** Ignores the {@code name} argument and returns {@code sourceData}. */
  @Override
  public Object access(Object sourceData, String name) throws RenderException {
    return sourceData;
  }
}
