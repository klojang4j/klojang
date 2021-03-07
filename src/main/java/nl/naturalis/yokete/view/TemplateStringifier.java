package nl.naturalis.yokete.view;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.view.ApplicationStringifier.Builder;
import nl.naturalis.yokete.view.ApplicationStringifier.TypeStringifier;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.notIn;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.check.CommonChecks.nullPointer;

/**
 * A {@code TemplateStringifier} is responsible for stringifying the values served up by the data
 * access layer. An {@code TemplateStringifier} does not, in fact, stringify any value itself.
 * Rather it is a container of {@link VariableStringifier} instances, specialized to stringify
 * values for a particular template variable.If the value can be stringified simply by calling
 * {@code toString()} on it, or to an empty {@code String} if the value is null, you don't need to
 * provide a stringifier for the variable because this is default behaviour. Also, if the way the
 * value is to be stringified doesn't really depend on the variable, but more generally on the type
 * of the value, you should register the stringifier with the {@link ApplicationStringifier}. Only
 * if a variable has specific stringification requirements should you create a {@code
 * VariableStringifier} and register it with the {@code TemplateStringifier}.
 *
 * <p>A {@code TemplateStringifier} is an immutable object. You should probably create just one
 * instance per {@link Template#ROOT_TEMPLATE_NAME root template} and keep it around for as lang as
 * your application lasts.
 *
 * @author Ayco Holleman
 */
public final class TemplateStringifier {

  /**
   * Stringifies the values for a particular template variable.
   *
   * @author Ayco Holleman
   */
  public static interface VariableStringifier {
    /**
     * Stringifies the specified value. Stringifier implementations <b>must</b> be able to handle
     * null values and the <b>must not</b> return null.
     *
     * @param tmplName The name of the template containing the variable having the specfied value
     * @param varName The name of the variable having the specified value
     * @param value The value to be stringified
     * @return A string represenation of the value
     * @throws RenderException
     */
    String stringify(String tmplName, String varName, Object value) throws RenderException;
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * A {@code Builder} class for {@code TemplateStringifier} instances.
   *
   * @author Ayco Holleman
   */
  public static final class Builder {

    private static final String CANNOT_STRINGIFY =
        "Type %s cannot be stringified by ApplicationStringifier";
    private static final String NO_APPLICATION_STRINGIFIER =
        "ApplicationStringifier must be set before assigning type to variable";
    private static final String NO_SUCH_VARIABLE = "No such variable: %s.%s";
    private static final String TYPE_ALREADY_SET = "Type already set for %s.%s";
    private static final String STRINGIFIER_ALREADY_SET = "Stringifier already set for %s.%s";

    private final Template tmpl;
    private final Set<Tuple<String, String>> varNames;

    private Builder(Template tmpl) {
      this.tmpl = tmpl;
      this.varNames = tmpl.getVariableNamesPerTemplate();
    }

    private ApplicationStringifier asf;
    private Map<Tuple<String, String>, Class<?>> varTypes = new HashMap<>();
    private Map<Tuple<String, String>, VariableStringifier> stringifiers = new HashMap<>();

    /**
     * Sets the {@link ApplicationStringifier} to be used for non-variable-specific stringification.
     *
     * @param stringifier The {@code ApplicationStringifier}
     * @return This {@code Builder} instance
     */
    public Builder setApplicationStringifier(ApplicationStringifier stringifier) {
      Check.that(asf).is(nullPointer(), "Application stringifier already set");
      asf = Check.notNull(stringifier).ok();
      return this;
    }

    /**
     * Sets the stringifier to be used for the specified variable. The variable is supposed to be
     * defined in the root template rather than in one the the templates nested inside it.
     *
     * @param varName The name of the variable for which to specify the stringifier
     * @param stringifier The stringifier
     * @return This {@code Builder} instance
     */
    public Builder setStringifier(String varName, VariableStringifier stringifier) {
      return setStringifier(tmpl.getName(), varName, stringifier);
    }

    /**
     * Sets the stringifier for the specified variable within the specified template.
     *
     * @param tmplName The template containing the variable, which <i>must</i> be a descendant of
     *     the root template for which the {@code TemplateStringifier} is being built. template for
     *     which the
     * @param varName The name of the variable for which to specify the stringifier
     * @param stringifier The stringifier
     * @return This {@code Builder} instance
     */
    public Builder setStringifier(
        String tmplName, String varName, VariableStringifier stringifier) {
      Check.notNull(tmplName, "templateName");
      Check.notNull(varName, "varName");
      Check.notNull(stringifier, "stringifier");
      Tuple<String, String> t = Tuple.of(tmplName, varName);
      Check.that(t)
          .is(in(), varNames, NO_SUCH_VARIABLE, tmplName, varName)
          .is(notIn(), stringifiers.keySet(), STRINGIFIER_ALREADY_SET, tmplName, varName)
          .is(notIn(), varTypes.keySet(), TYPE_ALREADY_SET, tmplName, varName)
          .then(x -> stringifiers.put(x, stringifier));
      return this;
    }

    /**
     * Sets the type of the objects the specified variable receives from the data access layer. The
     * variable is supposed to be defined in the root template rather than in one the the templates
     * nested inside it.
     *
     * @see #setType(String, String, Class)
     * @param varName The name of the variable for which to specify the stringifier
     * @param type The type of the variable
     * @return This {@code Builder} instance
     */
    public Builder setType(String varName, Class<?> type) {
      return setType(tmpl.getName(), varName, type);
    }

    /**
     * Sets the type of the objects the specified variable receives from the data access layer. This
     * serves two purposes:
     *
     * <ol>
     *   <li>The {@code TemplateStringifier} can request the appropriate stringifier from the {@link
     *       ApplicationStringifier} even if the value is null (in which case calling {@code
     *       getClass()} on it would result in a {@code NullPointerException}).
     *   <li>It gives you a way out if your application stringifies the same type in multiple ways.
     *       An example would be {@link LocalDateTime} objects that must be stringified differently
     *       in different parts of the application. See {@link TypeStringifier}.
     * </ol>
     *
     * @param tmplName The template containing the variable, which <i>must</i> be a descendant of
     *     the root template for which the {@code TemplateStringifier} is being built. template for
     *     which the
     * @param varName The name of the variable for which to specify the stringifier
     * @param type The type of the variable
     * @return This {@code Builder} instance
     */
    public Builder setType(String tmplName, String varName, Class<?> type) {
      Check.notNull(tmplName, "tmplName");
      Check.notNull(varName, "varName");
      Check.that(asf).is(notNull(), NO_APPLICATION_STRINGIFIER);
      Check.notNull(type, "type").is(asf::canStringify, CANNOT_STRINGIFY, type.getName());
      Check.that(Tuple.of(tmplName, varName))
          .is(in(), varNames, NO_SUCH_VARIABLE, tmplName, varName)
          .is(notIn(), stringifiers.keySet(), STRINGIFIER_ALREADY_SET, tmplName, varName)
          .is(notIn(), varTypes.keySet(), TYPE_ALREADY_SET, tmplName, varName)
          .then(tuple -> varTypes.put(tuple, type));
      return this;
    }

    /**
     * Returns a new, immutable {@code TemplateStringifier} instance.
     *
     * @return A new, immutable {@code TemplateStringifier} instance
     */
    public TemplateStringifier freeze() {
      varTypes.forEach(
          (tuple, type) -> {
            VariableStringifier vsf = asf.getStringifier(type).toVariableStringifier();
            stringifiers.put(tuple, vsf);
          });

      return null;
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@link Builder} object that lets you configure an {@code TemplateStringifier}
   * instance.
   *
   * @return A {@code Builder} object that lets you configure an {@code TemplateStringifier}
   *     instance
   */
  public static Builder forTemplate(Template rootTemplate) {
    return new Builder(rootTemplate);
  }

  private final Map<Tuple<String, String>, VariableStringifier> stringifiers;
  private final ApplicationStringifier asf;

  private TemplateStringifier(
      Map<Tuple<String, String>, VariableStringifier> stringifiers, ApplicationStringifier asf) {
    this.stringifiers = stringifiers;
    this.asf = asf;
  }

  public void stringify(String tmplName, String varName, Object value) {
    Check.notNull(tmplName, "tmplName");
    Check.notNull(varName, "varName");
    Tuple<String, String> t = Tuple.of(tmplName, varName);
  }
}
