package nl.naturalis.yokete.render;

import java.util.List;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;

/**
 * The {@code Page} class is a factory for {@link RenderSession render sessions}. Its main component
 * is the {@link Template} to be rendered. Besides that it contains an {@link AccessorFactory} that
 * provides {@link Accessor accessors} for the data fed into the template and a {@link
 * TemplateStringifiers} telling the {@code RenderSession} how to stringify values. The {@code Page}
 * class is not only a factory for render sessions, it also injects itself into the {@code
 * RenderSession} instances it creates, so they have access to each of these components.
 *
 * <p>Note that the name <i>Page</i> is somewhat misleading because an HTML template need not be a
 * full-blown HTML page. It can also be an HTML snippet that you insert into other pages.
 *
 * @author Ayco Holleman
 */
public final class Page {

  /**
   * Returns a {@code Page} that will produce {@link RenderSession} instances lacking an {@link
   * Accessor} and using the {@link Stringifier#DEFAULT default stringifier} to stringify values.
   *
   * @param template
   * @return
   */
  public static Page configure(Template template) {
    return configure(template, TemplateStringifiers.SIMPLE_STRINGIFIER);
  }

  /**
   * Returns a {@code Page} that will produce {@link RenderSession} instances lacking an {@link
   * Accessor}. That means you <i>cannot</i> call the {@link RenderSession#populate(String, Object,
   * String...) populate} and {@link RenderSession#insert(Object, EscapeType, String...) insert}
   * methods of {@code RenderSession} to populate the template. If you do, a {@link RenderException}
   * is thrown.
   *
   * @param template
   * @param stringifiers
   * @return
   */
  public static Page configure(Template template, TemplateStringifiers stringifiers) {
    Accessor<?> acc =
        (x, y) -> {
          throw RenderException.noAccessorProvided();
        };
    AccessorFactory af = (x, y) -> acc;
    return configure(template, af, stringifiers);
  }

  /**
   * Creates a {@code Page} that will produce {@link RenderSession render sessions} for the
   * specified template, using the specified {@code specified Accessor} to extract values from the
   * provided source data, and using the specified {@code TemplateStringifiers} to obtain
   * stringifiers that can stringify those values.
   *
   * @param template The template for which the {@code Page} will create render sessions
   * @param accessor The {@code Accessor} implementation to use for extracting values from source
   *     data
   * @param stringifiers The {@code TemplateStringifiers} instance to use for stringifying values.
   * @return A {@code Page} instance
   */
  public static Page configure(
      Template template, AccessorFactory accessor, TemplateStringifiers stringifiers) {
    Check.notNull(template);
    Check.notNull(accessor);
    Check.notNull(stringifiers);
    return new Page(template, accessor, stringifiers);
  }

  private final Template template;
  private final AccessorFactory accFactory;
  private final TemplateStringifiers stringifiers;

  /**
   * Initiates a new {@code RenderSession}.
   *
   * @return
   */
  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  private Page(Template template, AccessorFactory accFactory, TemplateStringifiers stringifiers) {
    this.template = template;
    this.accFactory = accFactory;
    this.stringifiers = stringifiers;
  }

  Accessor<?> getAccessor(Object sourceData) {
    return accFactory.getAccessor(sourceData.getClass(), template);
  }

  RenderSession newChildSession(Template nested) {
    Page factory = new Page(nested, accFactory, stringifiers);
    return factory.newRenderSession();
  }

  RenderSession newChildSession(Template nested, Accessor<?> acc) {
    Page factory = new Page(nested, (type, tmpl) -> acc, stringifiers);
    return factory.newRenderSession();
  }

  String[] stringify(String varName, List<?> values) throws RenderException {
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

  AccessorFactory getAccessorFactory() {
    return accFactory;
  }
}
