package nl.naturalis.yokete.render;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static java.util.Collections.emptyMap;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.common.check.CommonChecks.deepNotEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.yokete.template.TemplateUtils.getFQName;
import static nl.naturalis.yokete.template.TemplateUtils.getVarsPerTemplate;

/**
 * Provides {@link Stringifier stringifiers} for template variables. In principle every template
 * variable must be associated with a {@code Stringifier}. That includes not just the variables in
 * the template nominally being rendered, but also all variables in all templates nested inside it.
 * In practice, however, you are unlikely to define many template-specific stringifiers. If a
 * variable's value can be stringified by calling {@code toString()} on it (or to an empty string if
 * null), you don't need to specify a stringifier for it because this is {@link Stringifier#DEFAULT
 * default} behaviour. In addition, most variables can be stringified based solely on their data
 * type. These generic, type-based stringifiers are defined centrally, in the {@link
 * GlobalStringifiers} class. Only if a template variable has very specific stringification
 * requirements would you register the stringifier with a {@code TemplateStringifiers}.
 *
 * @author Ayco Holleman
 */
public final class TemplateStringifiers {

  /**
   * A simple, brute-force {@code TemplateStringifiers} instance that always returns the {@link
   * Stringifier#DEFAULT default stringifier}, whatever the template and whatever the variable.
   * Unlikely to be satisfactory in the end, but handy in the early stages of development.
   */
  public static final TemplateStringifiers SIMPLE_STRINGIFIER =
      new TemplateStringifiers(emptyMap(), GlobalStringifiers.EMPTY);

  /**
   * Returns a {@code TemplateStringifiers} instance which relies on global (type-based)
   * stringifiers only, doing completely without any template-specific and variable-specific
   * stringifiers.
   *
   * @param stringifiers The {@code GlobalStringifiers} instance providing the stringifiers
   * @return A {@code TemplateStringifiers} instance which relies on global (type-based)
   *     stringifiers only
   */
  public static TemplateStringifiers globalStringifiersOnly(GlobalStringifiers stringifiers) {
    return new TemplateStringifiers(emptyMap(), stringifiers);
  }

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * Lets you coonfigure a {@code TemplateStringifiers} instance.
   *
   * @author Ayco Holleman
   */
  public static class Builder {

    private static final String LOOKUP_FAILED = "No stringifier found for type %s";
    private static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";
    private static final String ALREADY_SET = "Stringifier already set for variable %s";

    private final Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>();

    private final Template template;
    private GlobalStringifiers globals;
    private final Set<Tuple<Template, String>> vars;

    private Builder(Template tmpl, GlobalStringifiers globals) {
      this.template = tmpl;
      this.globals = globals;
      this.vars = new HashSet<>(getVarsPerTemplate(tmpl));
    }

    /**
     * Associates the specified stringifier with the specified variables. The variables must be
     * declared in the template being configured rather than in one the templates nested inside it.
     * If a variable's value can be stringified using the {@link Stringifier#DEFAULT default
     * stringifier}, you don't need to call {@code register} for that variable.
     *
     * @param stringifier The stringifier
     * @param varNames The variables
     * @return This {@code Builder}
     */
    public Builder setStringifier(Stringifier stringifier, String... varNames) {
      return setStringifier(stringifier, template, varNames);
    }

    /**
     * Associates the specified stringifier with the specified variables within the specified
     * template. The specified {@code Template} must either be the {@code Template} that is being
     * configured or one of the templates nested inside it.
     *
     * @param stringifier The stringifier
     * @param template The template containing the variables
     * @param varNames The variables
     * @return This {@code Builder}
     */
    public Builder setStringifier(Stringifier stringifier, Template template, String... varNames) {
      Check.notNull(stringifier, "stringifier");
      Check.notNull(template, "template");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(stringifier, template, varNames);
      return this;
    }

    /**
     * Looks up a stringifier for the specified type in the {@link GlobalStringifiers} instance and
     * associates it with the specified variables. The variable must be declared in the template
     * being configured rather than in one the templates nested inside it. Only call this method if
     * a variable needs to be associated with a global stringifier other than the one already
     * associated with the <i>data type</i> of that variable. For example if all {@code LocalDate}
     * variables can be stringified using one date format, but there is one variable in one template
     * that requires a different date format, then you would call this method to cover that
     * particular variable.
     *
     * @param type The data type for which to retrieve a stringifier from the {@link
     *     GlobalStringifiers} instance (for example: a {@link LocalDate} stringifier)
     * @param varNames The name of the variables
     * @return This {@code Builder}
     */
    public Builder setGlobalStringifier(Class<?> type, String... varNames) {
      return addGlobalStringifier(type, template, varNames);
    }

    /**
     * Looks up a stringifier for the specified type in the {@link GlobalStringifiers} instance and
     * associates it with the specified variables. The specified {@code Template} must either be the
     * {@code Template} that is being configured or one of the templates nested inside it.
     *
     * @param type The type of the variable
     * @param template The template containing the variable
     * @param varName The name of the variable
     * @return This {@code Builder}
     */
    public Builder addGlobalStringifier(Class<?> type, Template template, String... varNames) {
      Check.notNull(type, "type").is(globals::hasStringifier, LOOKUP_FAILED, type.getName());
      Check.notNull(template, "template");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(globals.getStringifier(type), template, varNames);
      return this;
    }

    /**
     * Returns a new, immutable {@code TemplateStringifiers} instance.
     *
     * @return A new, immutable {@code TemplateStringifiers} instance
     */
    public TemplateStringifiers freeze() {
      return new TemplateStringifiers(Map.copyOf(stringifiers), globals);
    }

    private void doRegister(Stringifier stringifier, Template template, String... varNames) {
      for (String varName : varNames) {
        Tuple<Template, String> var = Tuple.of(template, varName);
        Check.that(var)
            .is(in(), vars, NO_SUCH_VARIABLE, getFQName(template, varName))
            .isNot(in(), stringifiers.keySet(), ALREADY_SET, getFQName(template, varName))
            .then(x -> stringifiers.put(x, stringifier));
      }
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@code Builder} instance that lets you configure a {@code TemplateStringifiers}
   * instance.
   *
   * @param template The template for which to define the stringifiers
   * @param globalStringifiers A {@code GlobalStringifiers} instance to retrieve type-based
   *     stringifiers from
   * @return A {@code Builder} instance that lets you configure a {@code TemplateStringifiers}
   *     instance
   */
  public static Builder configure(Template template, GlobalStringifiers globalStringifiers) {
    Check.notNull(template, "template");
    Check.notNull(globalStringifiers, "globalStringifiers");
    return new Builder(template, globalStringifiers);
  }

  private final Map<Tuple<Template, String>, Stringifier> stringifiers;
  private final GlobalStringifiers globals;

  private TemplateStringifiers(
      Map<Tuple<Template, String>, Stringifier> stringifiers, GlobalStringifiers globals) {
    this.stringifiers = stringifiers;
    this.globals = globals;
  }

  /**
   * Returns the stringifier to be used for the specified template variable.
   *
   * @param template The template containing the variable
   * @param varName The variable
   * @return The stringifier
   */
  public Stringifier getStringifier(Template template, String varName, Object value) {
    Tuple<Template, String> key = Tuple.of(template, varName);
    Stringifier stringifier = stringifiers.get(key);
    if (stringifier != null) {
      return stringifier;
    }
    if (value != null) {
      stringifier = globals.getStringifier(value.getClass());
    }
    return ifNull(stringifier, Stringifier.DEFAULT);
  }
}
