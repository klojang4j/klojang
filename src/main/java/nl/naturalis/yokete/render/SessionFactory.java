package nl.naturalis.yokete.render;

import java.util.List;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.render.RenderException.*;

/**
 * A factory class for {@link RenderSession} objects.
 *
 * @author Ayco Holleman
 */
public final class SessionFactory {

  /**
   * Creates a {@code SessionFactory} that will produce {@link RenderSession render sessions} for
   * the specified template, using the specified {@code specified Accessor} to extract values from
   * the provided source data, and using the specified {@code TemplateStringifiers} to obtain
   * stringifiers that can stringify those values.
   *
   * @param template The template for which the {@code SessionFactory} will create render sessions
   * @param accessor The {@code Accessor} implementation to use for extracting values from source
   *     data
   * @param stringifiers The {@code TemplateStringifiers} instance to use for stringifying values.
   * @return A {@code SessionFactory} instance
   */
  public static SessionFactory configure(
      Template template, Accessor<?> accessor, TemplateStringifiers stringifiers) {
    Check.notNull(template);
    Check.notNull(accessor);
    Check.notNull(stringifiers);
    return new SessionFactory(template, accessor, stringifiers);
  }

  private final Template template;
  private final Accessor<?> accessor;
  private final TemplateStringifiers stringifiers;

  /**
   * Initiates a new {@code RenderSession}.
   *
   * @return
   */
  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  private SessionFactory(
      Template template, Accessor<?> accessor, TemplateStringifiers stringifiers) {
    this.template = template;
    this.accessor = accessor;
    this.stringifiers = stringifiers;
  }

  RenderSession newChildSession(Template nestedTmpl, Object nestedData) throws RenderException {
    Accessor<?> acc = this.accessor.getAccessorForTemplate(nestedTmpl, nestedData);
    Check.on(nullAccessor(nestedTmpl), acc).is(notNull());
    SessionFactory factory = new SessionFactory(nestedTmpl, acc, stringifiers);
    return factory.newRenderSession();
  }

  RenderSession newChildSession(Template nestedTmpl, Accessor<?> acc) {
    SessionFactory factory = new SessionFactory(nestedTmpl, acc, stringifiers);
    return factory.newRenderSession();
  }

  String[] toString(String varName, List<?> values) throws RenderException {
    String[] strs = new String[values.size()];
    for (int i = 0; i < values.size(); ++i) {
      Stringifier stringifier = stringifiers.getStringifier(template, varName);
      try {
        if (null == (strs[i] = stringifier.toString(values.get(i)))) {
          throw BadStringifierException.stringifierReturnedNull(template, varName);
        }
      } catch (NullPointerException e) {
        throw BadStringifierException.stringifierNotNullResistant(template, varName);
      }
    }
    return strs;
  }

  Template getTemplate() {
    return template;
  }

  Accessor<?> getAccessor() {
    return accessor;
  }
}
