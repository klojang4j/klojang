package nl.naturalis.yokete.view;

/**
 * A generic interface for objects that mediate between the data layer and view layer. Its purpose
 * is to provide name-based access to the data in your model beans (or whatever it is the data layer
 * serves up). More precisely, its purpose is to map template variables to bean properties. In the
 * simplest case there is a one-to-one correspondenc between variable names and bean properties, but
 * this need not necessarily be so.
 *
 * <p>You can write your own {@code ViewData} implementations or use one of three implementations in
 * the {@link nl.naturalis.yokete.view.data} package.
 *
 * @author Ayco Holleman
 * @param <T> The type of objects that can be accessed by this {@code Accessor}
 */
public interface Accessor {

  public static final Object ABSENT = new Object();

  /**
   * Returns the value of the specified template variable within the specified object.
   * Implementations must distinguish between true {@code null} values and the variable not being
   * present in the source data at all. True {@code null} values are valid valid values that will be
   * stringified somehow by a {@link Stringifier} (e.g. to an empty string or to "&amp;nbsp;"). If
   * the variable could not be mapped to a bean property at all, this method must return the special
   * value {@link #ABSENT}. If a {@link RenderSession} receives this value for a particular
   * variable, it will assume you don't want to that particular variable just yet. (In the end, of
   * course, all template variables must be set before the template can be rendered.)
   *
   * @param from The object (supposedly) containing the value
   * @param varName The name of the template variable
   * @return The value of the variable
   */
  Object getValue(Object from, String varName) throws RenderException;

  Accessor getAccessorForNestedTemplate(String tmplName);
}
