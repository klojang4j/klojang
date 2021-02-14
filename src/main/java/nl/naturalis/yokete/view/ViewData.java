package nl.naturalis.yokete.view;

import java.util.List;
import java.util.Optional;

/**
 * A generic interface for objects that mediate between the data layer and viw layer. Its main
 * purpose is the stringify the model data served up by the data layer. The Yokete module is
 * agnostic about this is accomplished, but provides three {@code ViewData} implementations in the
 * {@code util} package.
 *
 * @author Ayco Holleman
 */
public interface ViewData {

  /**
   * Returns the value of the specified template variable. Implementations must distinguish between
   * true {@code null} values and the variable not being present in the source data at all. In the
   * latter case they must return an empty {@code Optional}. If the value served up by the data
   * layer is null, you can either decide to still return an empty {@code Optional}, or stringify
   * the null value to a non-null {@code String}. If the {@link Renderer} receives an empty {@code
   * Optional}, it will assume that you don't want to set the variable just yet. (In the end, of
   * course, all variables must have been substituted with actual values.)
   *
   * <p>The value of the variable comes in the form of a {@code List} of strings. If the list
   * contains more than one string, all strings are inserted adjacently at the location of the
   * variable. This can be useful if the variable represents not a simple, scalar value value but an
   * HTML snippet. For example, if you have this template:
   *
   * <p>
   *
   * <pre>
   *  &lt;html&gt;
   *    &lt;body&gt;
   *      &lt;table&gt;
   *        ~%rows%
   *      &lt;/table&gt;
   *    &lt;/body&gt;
   *  &lt;/html&gt;
   * </pre>
   *
   * <p>Then the @code ViewData} object could supply this value for the {@code rows} variable:
   *
   * <pre>
   * List.of("&lt;tr&gt;&lt;td&gt;ONE&lt;/td&gt;&lt;/tr&gt;",
   *  "&lt;tr&gt;&lt;td&gt;TWO&lt;/td&gt;&lt;/tr&gt;",
   *  "&lt;tr&gt;&lt;td&gt;THREE&lt;/td&gt;&lt;/tr&gt;")
   * </pre>
   *
   * <p>If the {@code List} is empty, the {@code Renderer} will replace the variable with an empty
   * string. This allows for conditional rendering. By supplying the {@code Renderer} with an empty
   * {@code List}, you effectively tell the {@code Renderer} to suppress rendering the variable.
   * Note that this is probably more useful when rendering {@link #getNestedViewData(Template,
   * String) nested templates}.
   *
   * <p>The {@code template} argument is the {@link Template} containing the variable.
   * Implementations could use it for pre-caching or other purposes, but are free to ignore it.
   *
   * @param template The {@code Template} that contains the variable
   * @param varName The name of the template variable
   * @return An {@code Optional} containing the stringified version of the variable's value or an
   *     empty {@code Optional}
   */
  Optional<List<String>> getValue(Template template, String varName) throws RenderException;

  /**
   * Returns a {@code ViewData} object containing the values for the specified <i>nested</i>
   * template. See {@link #getValue(Template, String) getValue} for the precise contract for this
   * method
   *
   * @param parent The {@code Template} that contains the specified nested template
   * @param chilTemplateName The name of the nested template
   * @return An {@code Optional} containing a nested {@code ViewData} object
   */
  Optional<List<ViewData>> getNestedViewData(Template parent, String chilTemplateName)
      throws RenderException;
}
