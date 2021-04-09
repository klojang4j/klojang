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
import static nl.naturalis.common.check.CommonChecks.deepNotEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.template.TemplateUtils.getFQName;

/**
 * Provides {@link Stringifier stringifiers} for template variables. In principle every template
 * variable must be associated with a {@code Stringifier}. That includes not just the variables in
 * the template nominally being rendered, but also all variables in all templates nested inside it.
 * In practice, though, you're likely to define very few template-specific stringifiers. If a
 * variable's value can be stringified by calling {@code toString()} on it, or to an empty string if
 * null, you don't need to specify a strinifier for the variable because this is {@link
 * Stringifier#DEFAULT default} behaviour. In addition, for most variables stringification does not
 * depend the variable per s&#233;, but rather on the variable's data type. These type-based
 * stringifiers are defined centrally, through the {@link GlobalStringifiers} class. (An example of
 * a type-based stringifier would be a {@link LocalDate} stringifier, or a {@link Number}
 * stringifier.) Only if a variable has very specific stringification requirements would you
 * register the stringifier with the {@code TemplateStringifier} class.
 *
 * @author Ayco Holleman
 */
public final class TemplateStringifiers {

  /**
   * A simple, brute-force {@code TemplateStringifiers} whose {@link #getStringifier(Template,
   * String) getStringifier} method always returns the {@link Stringifier#DEFAULT default
   * stringifier}, whatever the template, whatever the variable. Unlikely to be satisfactory in the
   * end, but handy in the early stages of development.
   */
  public static final TemplateStringifiers SIMPLETON = new TemplateStringifiers(emptyMap());

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * Lets you coonfigure a {@code TemplateStringifiers} instance.
   *
   * @author Ayco Holleman
   */
  public static class Builder {

    private static final String LOOKUP_FAILED = "No stringifier found for type %s";
    private static final String GSP_NOT_SET = "GlobalTemplateStringifiers not set";
    private static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";
    private static final String ALREADY_SET = "Stringifier already set for variable %s";

    private final Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>();

    private final Template template;
    private GlobalStringifiers globals;
    private final Set<Tuple<Template, String>> vars;

    private Builder(Template tmpl, GlobalStringifiers globals) {
      this.template = tmpl;
      this.globals = globals;
      this.vars = new HashSet<>(tmpl.getVarsPerTemplate());
    }

    /**
     * Associates the specified stringifier with the specified variables. The variables must be
     * declared in the template being configured (rather than in one the templates nested inside
     * it). If a variable's value can be stringified using the {@link Stringifier#DEFAULT default
     * stringifier}, you don't need to call {@code setStringifier} for that variable.
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
      register(stringifier, template, varNames);
      return this;
    }

    /**
     * Looks up a stringifier for the specified type in the {@link GlobalStringifiers} instance and
     * associates it with the specified variables. The variable must be declared in the template
     * being configured, rather than in one the templates nested inside it.
     *
     * @param type The data type for which to retrieve a stringifier from the {@link
     *     GlobalStringifiers} instance (for example: a {@link LocalDate} stringifier)
     * @param varNames The name of the variables
     * @return This {@code Builder}
     */
    public Builder addGlobalStringifier(Class<?> type, String... varNames) {
      return addGlobalStringifier(type, template, varNames);
    }

    /**
     * Looks up a stringifier for the specified type in the {@link GlobalStringifiers} instance and
     * associates it with the specified variables.
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
      Check.that(globals).is(notNull(), GSP_NOT_SET);
      register(globals.getStringifier(type), template, varNames);
      return this;
    }

    /**
     * Returns a new, immutable {@code TemplateStringifiers} instance.
     *
     * @return A new, immutable {@code TemplateStringifiers} instance
     */
    public TemplateStringifiers freeze() {
      return new TemplateStringifiers(Map.copyOf(stringifiers));
    }

    private void register(Stringifier stringifier, Template template, String... varNames) {
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
    return new Builder(template, globalStringifiers);
  }

  private final Map<Tuple<Template, String>, Stringifier> stringifiers;

  private TemplateStringifiers(Map<Tuple<Template, String>, Stringifier> stringifiers) {
    this.stringifiers = stringifiers;
  }

  /**
   * Returns the stringifier to be used for the specified template variable.
   *
   * @param template The template containing the variable
   * @param varName The variable
   * @return The stringifier
   */
  public Stringifier getStringifier(Template template, String varName) {
    Tuple<Template, String> key = Tuple.of(template, varName);
    return stringifiers.getOrDefault(key, Stringifier.DEFAULT);
  }
}