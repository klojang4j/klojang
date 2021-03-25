package nl.naturalis.yokete.accessors;

import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.EscapeType;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;
import nl.naturalis.yokete.template.Template;

/**
 * A non-accessor that does not access any value within the source data, but simply returns the
 * source data itself. Used by {@link RenderSession#fillMono(String, Object, EscapeType)}.
 *
 * @author Ayco Holleman
 */
public class SelfAccessor implements Accessor<Object> {

  public SelfAccessor() {}

  /** Returns {@code sourceData}. */
  @Override
  public Object access(Object sourceData, String name) throws RenderException {
    return sourceData;
  }

  /** Throws an {@link UnsupportedOperationException}. */
  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    String fmt = "%s does not support nested templates";
    String msg = String.format(fmt, getClass().getSimpleName());
    throw new UnsupportedOperationException(msg);
  }
}
