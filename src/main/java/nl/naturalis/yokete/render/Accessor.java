package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;

/**
 * A generic interface for objects that mediate between the data layer and view layer. Its purpose
 * is to provide name-based access to the data in your model beans (or whatever it is the data layer
 * serves up).
 *
 * <p>You can write your own {@code Accessor} implementations (likely one per model bean) or use one
 * of the Yokete implementations.
 *
 * @author Ayco Holleman
 */
public interface Accessor {

  public static final Object ABSENT = new Object();

  /**
   * Returns the value of the specified template variable within the specified object.
   *
   * <p>Implementations must distinguish between true {@code null} values and the variable not being
   * present in the source data at all. True {@code null} values are valid valid values that will be
   * stringified somehow by a {@link Stringifier} (e.g. to an empty string). If the variable
   * is not present in the source data, this method must return {@link #ABSENT}. If a {@link
   * RenderSession} receives this value for a particular variable, it will skip setting that
   * variable. In the end, of course, all template variables must be set before the template can be
   * rendered.
   *
   * @param data The object (supposedly) containing the value
   * @param varName The name of the template variable
   * @return The value of the variable
   */
  Object access(Object data, String varName) throws RenderException;

  /**
   * Returns an {@code Accessor} object that can access data destined for the specified nested
   * template.
   *
   * @param nested The nested template
   * @return An {@code Accessor} object that can access data destined for the specified nested
   *     template
   */
  Accessor getAccessorForTemplate(Template nested);
}
