package nl.naturalis.yokete.accessors;

import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;
import nl.naturalis.yokete.template.Template;

/**
 * A dummy {@code Accessor} implementation. Could be used when manually creating {@link
 * RenderSession#createChildSessions(String, Accessor, int) child sessions} for text-only templates,
 * or for templates that need to be populated in some bespoke way. The {@code BypassAccessor} throws
 * an {@link UnsupportedOperationException} from both methods declared by the {@code Accessor}
 * interface to ensure you don't implicitly rely on them being called.
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

  /** Throws an {@code UnsupportedOperationException}. */
  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    throw new UnsupportedOperationException(ERR);
  }
}
