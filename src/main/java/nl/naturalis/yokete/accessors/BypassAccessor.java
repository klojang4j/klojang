package nl.naturalis.yokete.accessors;

import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.template.Template;

/**
 * An
 *
 * @author Ayco Holleman
 */
public class BypassAccessor implements Accessor<Object> {

  public static final BypassAccessor BYPASS_ACCESSOR = new BypassAccessor();

  private static final String ERR = "Illegal reliance on BypassAccessor";

  private BypassAccessor() {}

  @Override
  public Object access(Object sourceData, String varOrNestedTemplateName) throws RenderException {
    throw new UnsupportedOperationException(ERR);
  }

  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    throw new UnsupportedOperationException(ERR);
  }
}
