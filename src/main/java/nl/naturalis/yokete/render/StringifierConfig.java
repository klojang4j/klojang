package nl.naturalis.yokete.render;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.notIn;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.check.CommonChecks.nullPointer;

/**
 * A factory class for {@link Stringifier} instances. A {@code StringifierFactory} always produces a
 * {@code Stringifier} specialized in stringifying values for a particular template. The {@code
 * Stringifier} instance it produces really only stringifies values by dispatching to other {@code
 * Stringifier} implementations, and the {@code StringifierFactory} lets you configure what those
 * implementations should be.
 *
 * <p>If a variable's value can be stringified simply by calling {@code toString()} on it, or to an
 * empty {@code String} if null, you don't need to provide a separate stringifier for the variable
 * because this is default behaviour. Also, if the way the value must be stringified depends only on
 * its type (and not on the variable to which it happns to be assigned), you should register the
 * stringifier with the {@link ApplicationStringifier}. Only if a variable has very specific
 * stringification requirements should you implement a {@code Stringifier} and register it with the
 * {@code StringifierFactory}.
 *
 * @author Ayco Holleman
 */
public final class StringifierConfig {

  private static final String CANNOT_STRINGIFY =
      "Type %s cannot be stringified by ApplicationStringifier";
  private static final String NO_APPLICATION_STRINGIFIER =
      "ApplicationStringifier must be set before assigning type to variable";
  private static final String NO_SUCH_VARIABLE = "No such variable: %s.%s";
  private static final String TYPE_ALREADY_SET = "Type already set for %s.%s";
  private static final String STRINGIFIER_ALREADY_SET = "Stringifier already set for %s.%s";

  private final Template tmpl;
  private final Set<Tuple<Template, String>> varNames;
  private final Map<Tuple<Template, String>, Class<?>> varTypes = new HashMap<>();
  private final Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>();

  private ApplicationStringifier appStringifier;

  StringifierConfig(Template tmpl) {
    this.tmpl = tmpl;
    this.varNames = new HashSet<>(tmpl.getVarsPerTemplate());
  }

  /**
   * Sets the {@link ApplicationStringifier} to be used for type-based, variable-independent
   * stringification.
   *
   * @param stringifier The {@code ApplicationStringifier}
   * @return This {@code StringifierFactory}
   */
  public StringifierConfig setApplicationStringifier(ApplicationStringifier stringifier) {
    Check.that(appStringifier).is(nullPointer(), "Application stringifier already set");
    appStringifier = Check.notNull(stringifier).ok();
    return this;
  }

  /**
   * Sets the stringifier for the specified variable. The variable must be declared in the template
   * being configured, rather than in one the templates nested inside it.
   *
   * @param varName The name of the variable
   * @param stringifier The stringifier
   * @return This {@code StringifierFactory}
   */
  public StringifierConfig register(String varName, Stringifier stringifier) {
    return register(tmpl, varName, stringifier);
  }

  /**
   * Sets the stringifier for the specified variable within the specified template. The specified
   * {@code Template} must either be the {@code Template} that is being configured or one of the
   * templates nested inside it.
   *
   * @param template The template containing the variable
   * @param varName The name of the variable
   * @param stringifier The stringifier
   * @return This {@code StringifierFactory}
   */
  public StringifierConfig register(Template template, String varName, Stringifier stringifier) {
    Check.notNull(template, "tmplName");
    Check.notNull(varName, "varName");
    Check.notNull(stringifier, "stringifier");
    Tuple<Template, String> t = Tuple.of(template, varName);
    Check.that(t)
        .is(in(), varNames, NO_SUCH_VARIABLE, template, varName)
        .is(notIn(), stringifiers.keySet(), STRINGIFIER_ALREADY_SET, template, varName)
        .is(notIn(), varTypes.keySet(), TYPE_ALREADY_SET, template, varName)
        .then(x -> stringifiers.put(x, stringifier));
    return this;
  }

  /**
   * Explicitly sets the type of the specified variable. The variable must be declared in the
   * template being configured, rather than in one the templates nested inside it.
   *
   * @see #setType(String, String, Class)
   * @param varName The name of the variable
   * @param type The type of the variable
   * @return This {@code StringifierFactory}
   */
  public StringifierConfig setType(String varName, Class<?> type) {
    return setType(tmpl, varName, type);
  }

  /**
   * Explicitly sets the type for the specified variable. This serves two purposes:
   *
   * <p>
   *
   * <ol>
   *   <li>It allows the {@code Stringifier} to request a generic, type-specific stringifier from
   *       the {@link ApplicationStringifier} even if the variable's value is null (in which case
   *       calling {@code value.getClass()} would cause a {@code NullPointerException}).
   *   <li>It lets you specify different application-level stringifiers for the same type. An
   *       example would be {@link LocalDateTime} objects that must be formatted differently in
   *       different parts of the application. See {@link ApplicationStringifier}.
   * </ol>
   *
   * <p>The specified {@code Template} must either be the {@code Template} that is being configured
   * or one of the templates descending from it.
   *
   * @param template The template containing the variable
   * @param varName The name of the variable
   * @param type The type of the variable
   * @return This {@code StringifierFactory}
   */
  public StringifierConfig setType(Template template, String varName, Class<?> type) {
    Check.notNull(template, "template");
    Check.notNull(varName, "varName");
    Check.that(appStringifier).is(notNull(), NO_APPLICATION_STRINGIFIER);
    Check.notNull(type, "type").is(appStringifier::canStringify, CANNOT_STRINGIFY, type.getName());
    Check.that(Tuple.of(template, varName))
        .is(in(), varNames, NO_SUCH_VARIABLE, template, varName)
        .is(notIn(), stringifiers.keySet(), STRINGIFIER_ALREADY_SET, template, varName)
        .is(notIn(), varTypes.keySet(), TYPE_ALREADY_SET, template, varName)
        .then(tuple -> varTypes.put(tuple, type));
    return this;
  }

  /**
   * Returns a new, immutable {@code Stringifier} instance.
   *
   * @return A new, immutable {@code Stringifier} instance
   */
  public Stringifier freeze() {
    // Swap template name and variable name swap because variable names have higher cardinality
    Map<Tuple<String, Template>, Stringifier> map0 = new HashMap<>(stringifiers.size());
    stringifiers.forEach((tuple, stringifier) -> map0.put(tuple.swap(), stringifier));
    Map<Tuple<String, Template>, Class<?>> map1 = new HashMap<>(varTypes.size());
    varTypes.forEach((tuple, type) -> map1.put(tuple.swap(), type));
    return new TemplateStringifier(map0, map1, appStringifier);
  }
}
