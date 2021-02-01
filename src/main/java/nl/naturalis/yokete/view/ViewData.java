package nl.naturalis.yokete.view;

/**
 * A generic interface for objects that mediate between the data layer and rendering layer.
 *
 * @author Ayco Holleman
 */
public interface ViewData {

  /**
   * The value to supply if the template variable passed to the {@link #get(String) get} method is
   * undefined in the data source.
   */
  public static final Object ABSENT = new Object();

  /**
   * Returns the value of the template variable with the specified name. Implementations should
   * distinguish between the variable having value {@code null}, and the variable not being present
   * at all. In the latter case they should return {@link #ABSENT} rather than throw an
   * exception.
   *
   * @param var The name of the template variable
   * @return Its value
   */
  Object get(String var);
}
