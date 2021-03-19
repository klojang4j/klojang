package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;

/**
 * A generic interface for objects that mediate between the data access layer and the view layer.
 * Its purpose is to provide name-based access to model beans (or whatever it is the data access
 * layer serves up).
 *
 * <p>The cleanest way to employ employ {@code Accessor} objects is to write them yourself, most
 * likely on a per-type basis. For example, you could have an {@code DepartmentAccessor} like this:
 *
 * <p>
 *
 * <pre>
 * public class DepartmentAccessor implements Accessor {
 *  public Object access(Object sourceData, String varName) {
 *
 *    if(sourceData.getClass() != Department.class) {
 *      return Accessor.UNDEFINED;
 *    }
 *    Department dept = (Department) sourceData;
 *    switch(varName) {
 *      case "name": return dept.getName();
 *      case "location": return dept.getLocation();
 *      case "managerId": return dept.getManagerId();
 *      case "employees": return dept.getEmployees();
 *      // etc.
 *      default: throw new IllegalArgumentException("No such property: " + varName);
 *    }
 *
 *    public getAccessorForTemplate(Template nestedTemplate, Object nestedObject) {
 *      if(nestedTemplate.getName().equals("employees") {
 *        return new EmployeeAccessor();
 *      }
 *      // etc.
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
public interface Accessor {

  /**
   * The value that must be returned if the variable whose value to retrieve is not present in the
   * source data.
   */
  public static final Object UNDEFINED = new Object();

  /**
   * Returns the value of the specified template variable within the specified object.
   *
   * <p>Implementations must distinguish between true {@code null} values and the variable not being
   * present in the source data at all. True {@code null} values are valid valid values that will be
   * stringified somehow by a {@link Stringifier} (e.g. to an empty string). If the variable is not
   * present in the source data, this method must return {@link #UNDEFINED}. If a {@link
   * RenderSession} receives this value for a particular variable, it will skip setting that
   * variable. In the end, of course, all template variables must be set before the template can be
   * rendered.
   *
   * @param sourceData The object (supposedly) containing the value
   * @param varName The name of the template variable
   * @return The value of the variable
   */
  Object access(Object sourceData, String varName) throws RenderException;

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
