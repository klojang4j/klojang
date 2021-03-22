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
   * the specified template, using the specified {@code specified Accessor} to retrieve values the
   * provided source data, and using the specified {@code Stringifier} to stringify those values so
   * they can be inserted into the template.
   *
   * @param template The template for which the {@code SessionFactory} will create render sessions.
   * @param accessor The {@code Accessor} implementation to use for obtaining values from the data
   *     passed to the various {@code RenderSession} methods.
   * @param stringifier The stringifier to use to stringify those values
   * @return A {@code SessionFactory} instance
   */
  public static SessionFactory configure(
      Template template, Accessor accessor, Stringifier stringifier) {
    Check.notNull(template);
    Check.notNull(accessor);
    Check.notNull(stringifier);
    return new SessionFactory(template, accessor, stringifier);
  }

  private final Template template;
  private final Accessor accessor;
  private final Stringifier stringifier;

  /**
   * Initiates a new {@code RenderSession}.
   *
   * @return
   */
  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  SessionFactory(Template template, Accessor accessor, Stringifier stringifier) {
    this.template = template;
    this.accessor = accessor;
    this.stringifier = stringifier;
  }

  RenderSession newChildSession(Template nestedTmpl, Object nestedData) throws RenderException {
    Accessor acc = accessor.getAccessorForTemplate(nestedTmpl, nestedData);
    Check.on(nullAccessor(nestedTmpl), acc).is(notNull());
    SessionFactory factory = new SessionFactory(nestedTmpl, acc, stringifier);
    return factory.newRenderSession();
  }

  RenderSession newChildSession(Template nestedTmpl, Accessor acc) {
    SessionFactory factory = new SessionFactory(nestedTmpl, acc, stringifier);
    return factory.newRenderSession();
  }

  String[] toString(String varName, List<?> values) throws RenderException {
    String[] strs = new String[values.size()];
    for (int i = 0; i < values.size(); ++i) {
      strs[i] = stringifier.toString(template, varName, values.get(i));
    }
    return strs;
  }

  Template getTemplate() {
    return template;
  }

  Accessor getAccessor() {
    return accessor;
  }

  Stringifier getStringifier() {
    return stringifier;
  }
}
