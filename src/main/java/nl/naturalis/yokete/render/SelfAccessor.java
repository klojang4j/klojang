package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;

/**
 * A non-accessor that does not access any value within the source data, but simply returns the
 * source data itself. Used by {@link RenderSession#fillMonoTemplate(String, Object, EscapeType)}.
 *
 * @author Ayco Holleman
 */
public class SelfAccessor implements Accessor<Object> {

  SelfAccessor() {}

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
