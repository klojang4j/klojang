package nl.naturalis.yokete.render;

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
import static nl.naturalis.common.check.CommonChecks.notIn;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.template.TemplateUtils.getFQName;

/**
 * Provides {@link Stringifier stringifiers} for template variables. In principle each and every
 * template variable needs to be associated with a {@code Stringifier}. However, each variable
 * implicitly already <i>is</i> associated with a stringifier: the {@link Stringifier#DEFAULT
 * default stringifier}. You can use the {@link StringifierProvider.Builder} to configure
 * alternative stringifier.
 *
 * @author Ayco Holleman
 */
public final class StringifierProvider {

  /**
   * A simple, brute-force {@code StringifierProvider} whose {@link #getStringifier(Template,
   * String) getStringifier} method always returns the {@link Stringifier#DEFAULT default
   * stringifier}, whatever the template, whatever the variable. Unlikely to be satisfactory in the
   * end, but handy in the early stages of development.
   */
  public static final StringifierProvider SIMPLETON = new StringifierProvider(emptyMap());

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * Lets you set up a {@code StringifierProvider}.
   *
   * @author Ayco Holleman
   */
  public static class Builder {

    private static final String LOOKUP_FAILED = "No stringifier found for type %s";
    private static final String GSP_NOT_SET = "GlobalStringifierProvider not set";
    private static final String NO_SUCH_VARIABLE = "No such variable: \"%s\"";
    private static final String ALREADY_SET = "Stringifier already set for variable %s";

    private final Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>();

    private final Template tmpl;
    private GlobalStringifierProvider gsp;
    private final Set<Tuple<Template, String>> vars;

    private Builder(Template tmpl, GlobalStringifierProvider gsp) {
      this.tmpl = tmpl;
      this.gsp = gsp;
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
      return setStringifier(stringifier, tmpl, varNames);
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
     * Retrieves a stringifier for the specified type from the {@link GlobalStringifierProvider} and
     * associates it with the specified variables. The variable must be declared in the template
     * being configured, rather than in one the templates nested inside it.
     *
     * @see #setType(String, String, Class)
     * @param varName The name of the variable
     * @param type The type of the variable
     * @return This {@code Builder}
     */
    public Builder lookupGlobal(Class<?> type, String... varNames) {
      return lookupGlobal(type, tmpl, varNames);
    }

    /**
     * Retrieves a stringifier for the specified type from the {@link GlobalStringifierProvider} and
     * associates it with the specified variables.
     *
     * @param type The type of the variable
     * @param template The template containing the variable
     * @param varName The name of the variable
     * @return This {@code Builder}
     */
    public Builder lookupGlobal(Class<?> type, Template template, String... varNames) {
      Check.notNull(type, "type").is(gsp::hasStringifier, LOOKUP_FAILED, type.getName());
      Check.notNull(template, "template");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      Check.that(gsp).is(notNull(), GSP_NOT_SET);
      register(gsp.getStringifier(type), template, varNames);
      return this;
    }

    /**
     * Returns a new, immutable {@code StringifierProvider} instance.
     *
     * @return A new, immutable {@code StringifierProvider} instance
     */
    public StringifierProvider freeze() {
      return new StringifierProvider(Map.copyOf(stringifiers));
    }

    private void register(Stringifier stringifier, Template template, String... varNames) {
      for (String varName : varNames) {
        Tuple<Template, String> var = Tuple.of(template, varName);
        Check.that(var)
            .is(in(), vars, NO_SUCH_VARIABLE, getFQName(template, varName))
            .is(notIn(), stringifiers.keySet(), ALREADY_SET, getFQName(template, varName))
            .then(x -> stringifiers.put(x, stringifier));
      }
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  public static Builder configure(Template template, GlobalStringifierProvider gsp) {
    return new Builder(template, gsp);
  }

  private final Map<Tuple<Template, String>, Stringifier> stringifiers;

  private StringifierProvider(Map<Tuple<Template, String>, Stringifier> stringifiers) {
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
