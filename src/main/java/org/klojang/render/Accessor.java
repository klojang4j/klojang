package org.klojang.render;

import org.klojang.template.Template;

/**
 * Interface for objects that mediate between the data access layer and the view layer. Its purpose
 * is to provide name-based access to model beans, or whatever it is the data access layer serves
 * up).
 *
 * <p>The cleanest way to employ employ {@code Accessor} objects is to write them yourself, most
 * likely on a per-type basis. For example, you could have an {@code DepartmentAccessor} like this:
 *
 * <p>
 *
 * <pre>
 * public class DepartmentAccessor implements Accessor {
 *  public Object access(Object sourceData, String varName) {
 *    Department dept = (Department) sourceData;
 *    switch(varName) {
 *      case "name": return dept.getName();
 *      case "location": return dept.getLocation();
 *      case "managerId": return dept.getManagerId();
 *      case "employees": return dept.getEmployees();
 *      // etc.
 *      default: throw new IllegalArgumentException("No such property: " + varName);
 *    }
 *  }
 * }
 * </pre>
 *
 * <p>The advantage of writing your own {@code Accessor} implementations, as can be seen from the
 * {@code default} case in the above {@code switch} statement, is that spelling errors in your
 * templates immediately reveal themselves. In addition, these implementations completely do without
 * reflection or dynamic invocation. Taken together, these advantages give a strong sense of control
 * to tailor-made {@code Accessor} implementations.
 *
 * <p>Nevertheless, if you are not too bothered about these considerations, Yokete does provide some
 * {@code Accessor} implementations that you can use as a matter of convenience.
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface Accessor<T> {

  /**
   * The value that must be returned if the variable whose value to retrieve is not present in the
   * source data.
   */
  public static final Object UNDEFINED = new Object();

  /**
   * Returns the value of the specified template variable or nested template within the specified
   * object. Generally speaking the returned value will have a relatively simple type for variables
   * (e.g. a {@code String}, {@code Integer} or {@code LocalDate}, while it will be a complex object
   * for a nested template, since it will function as the source data for that template. Together
   * with the nested template itself this source data object will be passed to {@link
   * #getAccessorForTemplate(Template, Object) getAccessorForTemplate}, so the {@link RenderSession}
   * can extract values from it.
   *
   * <p>Implementations must distinguish between true {@code null} values and the variable not being
   * present in the source data at all. True {@code null} values are valid valid values that will be
   * stringified somehow by a {@link Stringifier} (e.g. to an empty string). If the variable is not
   * present in the source data, this method must return {@link #UNDEFINED}. If a {@code
   * RenderSession} receives this value for a particular variable, it will skip setting that
   * variable. On the other hand, if it receives {@code null} from the {@code Accessor} while the
   * variable has already been set, it will throw a {@link RenderException}.
   *
   * @param sourceData The object (supposedly) containing the value
   * @param varOrNestedTemplateName The name of the template variable or nested template
   * @return The value of the variable
   */
  Object access(T sourceData, String varOrNestedTemplateName) throws RenderException;
}
