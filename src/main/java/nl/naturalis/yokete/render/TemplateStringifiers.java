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
 * Provides {@link Stringifier stringifiers} for template variables. In principle each and every
 * template variable must be associated with a {@code Stringifier}. In practice, however, it is
 * unlikely you will define many template-specific stringifiers. If a variable's value can be
 * stringified by calling {@code toString()} on it (or to an empty string if null), you don't need
 * to specify a stringifier for it because this is default behaviour (see {@link
 * Stringifier#DEFAULT}). In addition, most variables can be stringified based solely on their data
 * type. These type-based stringifiers are defined centrally, in a {@link TypeStringifiers}
 * instance. Only if a template variable has very specific stringification requirements would you
 * register the stringifier with a {@code TemplateStringifiers} instance.
 *
 * <p>This is how a {@link RenderSession} decides which stringifier to use for a template variable:
 *
 * <p>
 *
 * <ol>
 *   <li>If a stringifier has been defined for that particular variable in that particular template,
 *       then that is the stringifier that is going to be used.
 *   <li>If a stringifier has been defined for all variables with that particular name (irrespective
 *       of which templates contain them), then that is the stringifier that is going to be used.
 *       See {@link Builder#setNameBasedStringifier(Stringifier, String...)
 *       setNameBasedStringifier}.
 *   <li>If a stringifier has been defined for the data type of that particular variable, then that
 *       is the stringifier that is going to be used.
 *   <li>If you have defined your own default stringifier, then that is the stringifier that is
 *       going to be used.
 *   <li>Otherwise {@link Stringifier#DEFAULT} is going to be used.
 * </ol>
 *
 * @author Ayco Holleman
 */
public final class TemplateStringifiers {

  /**
   * A simple, brute-force {@code TemplateStringifiers} instance that always returns the {@link
   * Stringifier#DEFAULT default stringifier}, whatever the template and whatever the variable.
   * Unlikely to be satisfactory in the end, but handy in the early stages of development.
   */
  public static final TemplateStringifiers BASIC_STRINGIFIER =
      new TemplateStringifiers(emptyMap(), TypeStringifiers.EMPTY, null);

  /**
   * Returns {@link #BASIC_STRINGIFIER}. Can be used to build up a {@code TemplateStringifiers}
   * using the {@code withXXX} methods (rather than via a {@link Builder} instance).
   *
   * @return {@link #BASIC_STRINGIFIER}
   */
  public static TemplateStringifiers basic() {
    return BASIC_STRINGIFIER;
  }

  /**
   * Returns a {@code TemplateStringifiers} instance which relies soley on type-based stringifiers
   * and the {@link Stringifier#DEFAULT default stringifier}. This might already satisfy all your
   * stringification requirements.
   *
   * @param stringifiers The {@code TypeStringifiers} instance providing the stringifiers
   * @return A {@code TemplateStringifiers} instance which relies on global (type-based)
   *     stringifiers only
   */
  public static TemplateStringifiers usingTypeStringifiers(TypeStringifiers stringifiers) {
    return new TemplateStringifiers(emptyMap(), stringifiers, null);
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
    private final TypeStringifiers typeStringifiers;
    private final Set<Tuple<Template, String>> vars;

    private Stringifier defStringifier;

    private Builder(Template tmpl, TypeStringifiers typeStringifiers) {
      this.template = tmpl;
      this.typeStringifiers = typeStringifiers;
      this.vars = new HashSet<>(getVarsPerTemplate(tmpl));
    }

    /**
     * Lets you specifiy your own default stringifier, replacing {@link Stringifier#DEFAULT}.
     *
     * @param stringifier The default stringifier to use
     * @return This {@code Builder}
     */
    public Builder setDefaultStringifier(Stringifier stringifier) {
      this.defStringifier = stringifier;
      return this;
    }

    /**
     * Associates the specified stringifier with the specified variables. The variables must be
     * declared in the template being configured rather than in one of the templates nested inside
     * it. If a variable's value can be stringified using the {@link Stringifier#DEFAULT default
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
     * Associates the specified stringifier with the specified variable names, irrespective of which
     * templates contain them. This may be useful to stringify consistently named variables (like
     * "dateModified" or "firstName"). Template-specific stringifiers take precedence over
     * name-based stringifiers while name-based stringifiers take precedence over global
     * stringifiers.
     *
     * @param stringifier The stringifier
     * @param varNames The variable names to associate the stringifier with.
     * @return This {@code Builder}
     */
    public Builder setNameBasedStringifier(Stringifier stringifier, String... varNames) {
      Check.notNull(stringifier, "stringifier");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(stringifier, null, varNames);
      return this;
    }

    /**
     * Looks up the stringifier for the specified type in the {@link TypeStringifiers} instance and
     * associates it with the specified variables. The variable must be declared in the template
     * being configured rather than in one of the templates nested inside it. Only call this method
     * if a variable needs to be associated with a global stringifier other than the one already
     * associated with the data type of that variable. For example if all {@code LocalDate}
     * variables can be stringified using one date format, but there is one variable in one template
     * that requires a different date format, then you would call this method to cover that
     * particular variable.
     *
     * @param type The data type for which to retrieve a stringifier from the {@link
     *     TypeStringifiers} instance (for example: a {@link LocalDate} stringifier)
     * @param varNames The name of the variables
     * @return This {@code Builder}
     */
    public Builder setGlobalStringifier(Class<?> type, String... varNames) {
      return setGlobalStringifier(type, template, varNames);
    }

    /**
     * Looks up a stringifier for the specified type in the {@link TypeStringifiers} instance and
     * associates it with the specified variables. The specified {@code Template} must either be the
     * {@code Template} that is being configured or one of the templates nested inside it.
     *
     * @param type The type of the variable
     * @param template The template containing the variable
     * @param varName The name of the variable
     * @return This {@code Builder}
     */
    public Builder setGlobalStringifier(Class<?> type, Template template, String... varNames) {
      Check.notNull(type, "type")
          .is(typeStringifiers::hasStringifier, LOOKUP_FAILED, type.getName());
      Check.notNull(template, "template");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      doRegister(typeStringifiers.getStringifier(type), template, varNames);
      return this;
    }

    /**
     * Returns a new, immutable {@code TemplateStringifiers} instance.
     *
     * @return A new, immutable {@code TemplateStringifiers} instance
     */
    public TemplateStringifiers freeze() {
      return new TemplateStringifiers(Map.copyOf(stringifiers), typeStringifiers, defStringifier);
    }

    private void doRegister(Stringifier stringifier, Template template, String... varNames) {
      for (String varName : varNames) {
        Tuple<Template, String> var = Tuple.of(template, varName);
        if (template == null) {
          Check.that(var).isNot(in(), stringifiers.keySet(), ALREADY_SET, varName);
        } else {
          Check.that(var)
              .is(in(), vars, NO_SUCH_VARIABLE, getFQName(template, varName))
              .isNot(in(), stringifiers.keySet(), ALREADY_SET, getFQName(template, varName));
        }
        stringifiers.put(var, stringifier);
      }
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@code Builder} instance that lets you configure the stringifiers for a template.
   *
   * @param template The template for which to define the stringifiers
   * @param globalStringifiers A {@code TypeStringifiers} instance to retrieve type-based
   *     stringifiers from
   * @return A {@code Builder} instance that lets you configure a {@code TemplateStringifiers}
   *     instance
   */
  public static Builder configure(Template template, TypeStringifiers globalStringifiers) {
    Check.notNull(template, "template");
    Check.notNull(globalStringifiers, "globalStringifiers");
    return new Builder(template, globalStringifiers);
  }

  private final Map<Tuple<Template, String>, Stringifier> stringifiers;
  private final TypeStringifiers typeStringifiers;
  private final Stringifier defStringifier;

  private TemplateStringifiers(
      Map<Tuple<Template, String>, Stringifier> stringifiers,
      TypeStringifiers typeStringifiers,
      Stringifier defStringifier) {
    this.stringifiers = stringifiers;
    this.typeStringifiers = typeStringifiers;
    this.defStringifier = defStringifier;
  }

  /**
   * Returns a new {@code TemplateStringifiers} instance enriched with the specified name based
   * stringifier. See {@link Builder#setNameBasedStringifier(Stringifier, String...)
   * Builder.setNameBasedStringifier}.
   *
   * @param varName
   * @param stringifier
   * @return
   */
  public TemplateStringifiers withNameBasedStringifier(String varName, Stringifier stringifier) {
    Check.notNull(varName, "varName");
    Check.notNull(stringifier, "stringifier");
    Tuple<Template, String> t = Tuple.of(null, varName);
    Check.that(t).isNot(in(), stringifiers.keySet(), Builder.ALREADY_SET, varName);
    Map<Tuple<Template, String>, Stringifier> stringifiers = new HashMap<>(this.stringifiers);
    stringifiers.put(t, stringifier);
    return new TemplateStringifiers(stringifiers, typeStringifiers, defStringifier);
  }

  public TemplateStringifiers withTypeStringifiers(TypeStringifiers typeStringifiers) {
    Check.notNull(typeStringifiers, "typeStringifiers");
    return new TemplateStringifiers(stringifiers, typeStringifiers, defStringifier);
  }

  public TemplateStringifiers withDefaultStringifiers(Stringifier defaultStringifier) {
    Check.notNull(defaultStringifier, "defaultStringifier");
    return new TemplateStringifiers(stringifiers, typeStringifiers, defaultStringifier);
  }

  /**
   * Returns the stringifier to be used for the specified template variable.
   *
   * @param template The template containing the variable
   * @param varName The variable
   * @return The stringifier
   */
  public Stringifier getStringifier(Template template, String varName, Object value) {
    Check.notNull(template, "template");
    Check.notNull(varName, "varName");
    Tuple<Template, String> key = Tuple.of(template, varName);
    Stringifier stringifier = stringifiers.get(key);
    if (stringifier != null) {
      return stringifier;
    }
    key = Tuple.of(null, varName);
    stringifier = stringifiers.get(key);
    if (stringifier != null) {
      return stringifier;
    }
    if (value != null) {
      stringifier = typeStringifiers.getStringifier(value.getClass());
    }
    if (defStringifier != null) {
      return defStringifier;
    }
    return ifNull(stringifier, Stringifier.DEFAULT);
  }
}
