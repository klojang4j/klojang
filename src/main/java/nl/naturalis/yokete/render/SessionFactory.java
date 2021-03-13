package nl.naturalis.yokete.render;

import java.util.ArrayList;
import java.util.List;
import nl.naturalis.common.CollectionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;

/**
 * A factory class for {@link RenderSession} objects.
 *
 * @author Ayco Holleman
 */
public final class SessionFactory {

  /**
   * Configures and returns a {@code SessionFactory}.
   *
   * @param template The template for which the {@code SessionFactory} will create render sessions.
   * @param accessor The {@code Accessor} implementation to use for obtaining values from the data
   *     passed to the various {@code RenderSession} methods.
   * @param stringifier The stringifier to use to stringify those values
   * @return A {@code SessionFactory} instance
   */
  public static SessionFactory configure(
      Template template, Accessor accessor, TemplateStringifier stringifier) {
    return new SessionFactory(template, accessor, stringifier);
  }

  private final Template template;
  private final Accessor accessor;
  private final TemplateStringifier stringifier;

  /**
   * Initiates a new {@code RenderSession}.
   *
   * @return
   */
  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  SessionFactory(Template template, Accessor accessor, TemplateStringifier stringifier) {
    this.template = Check.notNull(template).ok();
    this.accessor = Check.notNull(accessor).ok();
    this.stringifier = Check.notNull(stringifier).ok();
  }

  RenderSession newChildSession(Template nested) {
    Accessor acc = accessor.getAccessorForTemplate(nested);
    SessionFactory factory = new SessionFactory(nested, acc, stringifier);
    return factory.newRenderSession();
  }

  List<String> stringify(Object data, String varName) throws RenderException {
    Object value = accessor.access(data, varName);
    List<?> values = CollectionMethods.asList(value);
    List<String> strvals = new ArrayList<>(values.size());
    for (Object val : values) {
      String strval = stringifier.stringify(template, varName, val);
      strvals.add(strval);
    }
    return strvals;
  }

  Template getTemplate() {
    return template;
  }

  Accessor getAccessor() {
    return accessor;
  }

  TemplateStringifier getStringifier() {
    return stringifier;
  }
}
