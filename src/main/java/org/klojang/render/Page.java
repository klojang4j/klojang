package org.klojang.render;

import org.klojang.template.Template;
import nl.naturalis.common.check.Check;
import static org.klojang.render.StringifierFactory.BASIC_STRINGIFIERS;
import static org.klojang.render.AccessorFactory.*;

/**
 * A {@code Page} is a factory for {@link RenderSession render sessions}. Its main component is the
 * {@link Template} to be rendered. Besides that it contains an {@link AccessorFactory} that
 * provides {@link Accessor accessors} for the data fed into the template and a {@link
 * StringifierFactory} telling the {@code RenderSession} how to stringify values.
 *
 * <p>Note that the name <i>Page</i> is somewhat misleading because an HTML template need not be a
 * full-blown HTML page. It can also be an HTML snippet that you include or {@link
 * RenderSession#paste(String, Renderable) "paste"} into other pages. Or it can be something else
 * completely.
 *
 * @author Ayco Holleman
 */
public final class Page {

  /**
   * Returns a {@code Page} that will produce {@link RenderSession render sessions} that only use
   * {@link AccessorFactory#BASIC_ACCESSORS predefined accessors} and {@link StringifierFactory
   * predefined stringifiers} to extract resp. stringify values from source data objects.
   *
   * @param template The template to be populated
   * @return A new {@code Page}
   */
  public static Page configure(Template template) {
    return configure(template, BASIC_ACCESSORS, BASIC_STRINGIFIERS);
  }

  /**
   * Returns a {@code Page} that will produce {@link RenderSession render sessions} that only use
   * {@link AccessorFactory#BASIC_ACCESSORS predefined accessors} to extract values from source data
   * objects and {@link StringifierFactory#BASIC_STRINGIFIERS predefined stringifiers}, and that
   * will use the specified {@code StringifierFactory} to get hold of stringifiers for those values.
   *
   * @param template The template to be populated
   * @param The {@code StringifierFactory}
   * @return A new {@code Page}
   */
  public static Page configure(Template template, StringifierFactory stringifiers) {
    return configure(template, BASIC_ACCESSORS, stringifiers);
  }

  /**
   * Creates a {@code Page} that will produce {@link RenderSession render sessions} for the
   * specified template, using the specified {@code AccessorFactory} to produce {@link Accessor
   * accessors} for source data for the template, and using the {@link
   * StringifierFactory#BASIC_STRINGIFIERS default stringifier} to stringify the source data.
   *
   * @param template The template to be populated
   * @param accessors The {@code AccessorFactory}
   * @return A new {@code Page}
   */
  public static Page configure(Template template, AccessorFactory accessors) {
    return configure(template, accessors, BASIC_STRINGIFIERS);
  }

  /**
   * Returns a {@code Page} that will produce {@link RenderSession render sessions} that use the
   * specified {@code AccessorFactory} to get hold of accessors for source data objects, and that
   * will use the specified {@code StringifierFactory} to get hold of stringifiers for those values.
   *
   * @param template The template for which the {@code Page} will create render sessions
   * @param accessors The {@code Accessor} implementation to use for extracting values from source
   *     data
   * @param stringifiers The {@code StringifierFactory} instance to use for stringifying values.
   * @return A {@code Page} instance
   */
  public static Page configure(
      Template template, AccessorFactory accessors, StringifierFactory stringifiers) {
    Check.notNull(template);
    Check.notNull(accessors);
    Check.notNull(stringifiers);
    return new Page(template, accessors, stringifiers);
  }

  private final Template template;
  private final AccessorFactory accFactory;
  private final StringifierFactory stringifiers;

  /**
   * Initiates a new {@code RenderSession}.
   *
   * @return
   */
  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  private Page(Template template, AccessorFactory accFactory, StringifierFactory stringifiers) {
    this.template = template;
    this.accFactory = accFactory;
    this.stringifiers = stringifiers;
  }

  public Template getTemplate() {
    return template;
  }

  public AccessorFactory getAccessorFactory() {
    return accFactory;
  }

  public StringifierFactory getStringifierFactory() {
    return stringifiers;
  }

  Accessor<?> getAccessor(Object sourceData) {
    return accFactory.getAccessor(sourceData.getClass(), template);
  }

  RenderSession newChildSession(Template nested) {
    Page factory = new Page(nested, accFactory, stringifiers);
    return factory.newRenderSession();
  }
}
