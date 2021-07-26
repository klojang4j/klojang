package org.klojang.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.apache.commons.text.StringEscapeUtils;
import org.klojang.template.Template;
import org.klojang.template.TemplateUtils;
import org.klojang.template.VarGroup;
import org.klojang.template.VariablePart;
import org.klojang.x.template.XVarGroup;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import static org.klojang.template.VarGroup.HTML;
import static org.klojang.template.VarGroup.JS;
import static org.klojang.template.VarGroup.TEXT;
import static nl.naturalis.common.StringMethods.EMPTY;
import static nl.naturalis.common.StringMethods.ltrim;
import static nl.naturalis.common.StringMethods.rtrim;
import static nl.naturalis.common.StringMethods.trim;
import static nl.naturalis.common.check.CommonChecks.deepNotEmpty;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.keyIn;

/**
 * Provides {@link Stringifier stringifiers} for template variables. In principle each and every
 * template variable must be associated with a {@code Stringifier}. In practice, however, it is
 * unlikely you will define many variable-specific stringifiers. If a variable's value can be
 * stringified by calling {@code toString()} on it (or to an empty string if null), you don't need
 * to specify a stringifier for it because this is default behaviour. In addition, all variables
 * with the same data type will usually also have to be stringified in the same way. (For example
 * you may want to format all integers according to your country's locale.) These generic,
 * type-based stringifiers can be configured using {@link Builder#addTypeBasedStringifier(String...,
 * Class) Builder.setTypeStringifier}. Only if a template variable has very specific stringification
 * requirements would you register the stringifier using {@link Builder#add(Stringifier, String...)
 * Builder.setStringifier}.
 *
 * <p>Type-based stringifiers are internally kept in a {@link TypeMap}. This means that if a
 * stringifier is requested for some type, and that type is not in the {@code TypeMap}, but one of
 * its super types is, then you get the stringifier associated with the super type. For example, if
 * the {@code TypeMap} contains a {@code Number} stringifier and you request an {@code Integer}
 * stringifier, you get the {@code Number} stringifier (unless you have also added an {@code
 * Integer} stringifier to the {@code TypeMap}). This saves you from having to specify a stringifier
 * for every subclass of {@code Number} if they are all stringified in the same way.
 *
 * <p>This is how a {@link StringifierFactory} decides which stringifier to hand out for a template
 * variable:
 *
 * <p>
 *
 * <ol>
 *   <li>If a stringifier has been defined for a {@link XVarGroup variable group} and the variable
 *       belongs to that group, then that is the stringifier that is going to be used.
 *   <li>If a stringifier has been defined for that particular variable in that particular template,
 *       then that is the stringifier that is going to be used.
 *   <li>If a stringifier has been defined for all variables with that particular name (irrespective
 *       of which template they belong to), then that is the stringifier that is going to be used.
 *       See {@link Builder#addNameBasedStringifier(String..., Stringifier)
 *       setNameBasedStringifier}.
 *   <li>If a stringifier has been defined for the data type of that particular variable, then that
 *       is the stringifier that is going to be used.
 *   <li>If you have defined your own default stringifier, then that is the stringifier that is
 *       going to be used.
 *   <li>Otherwise the {@link Stringifier#DEFAULT default stringifier} is going to be used.
 * </ol>
 *
 * @see SessionConfig
 * @author Ayco Holleman
 */
public final class StringifierFactory {

  /**
   * A simple, brute-force {@code StringifierFactory} instance that always returns the {@link
   * Stringifier#DEFAULT default stringifier}, whatever the template and whatever the variable.
   * Unlikely to be satisfactory in the end, but handy in the early stages of development.
   */
  public static final StringifierFactory STANDARD_STRINGIFIERS = configure().freeze();

  /* ++++++++++++++++++++[ BEGIN BUILDER CLASS ]+++++++++++++++++ */

  /**
   * Lets you configure a {@code StringifierFactory} instance for a template. If you don't require
   * any template-specific stringifiers, you can also start out using {@link
   * StringifierFactory#basic()}.
   *
   * @author Ayco Holleman
   */
  public static class Builder {

    private static final String ERR_VAR_ASSIGNED = "Stringifier already set for variable \"%s\"";
    private static final String ERR_GROUP_ASSIGNED = "Stringifier already set for group \"%s\"";
    private static final String ERR_TYPE_ASSIGNED = "Stringifier already set for type \"%s\"";
    private static final String ERR_TYPE_SET = "Data type already set for variable \"%s\"";

    private Stringifier defStringifier = Stringifier.DEFAULT;

    private final Map<StringifierId, Stringifier> stringifiers = new HashMap<>();
    private final Map<Class<?>, Stringifier> typeStringifiers = new HashMap<>();
    private final Map<Tuple<Template, String>, Class<?>> typeLookup = new HashMap<>();
    private final List<Tuple<String, Stringifier>> partialNames = new ArrayList<>();

    private Builder() {
      stringifiers.put(new StringifierId(TEXT), Stringifier.DEFAULT);
      stringifiers.put(new StringifierId(HTML), wrap(StringEscapeUtils::escapeHtml4));
      stringifiers.put(new StringifierId(JS), wrap(StringEscapeUtils::escapeEcmaScript));
    }

    private static Stringifier wrap(UnaryOperator<String> stringifier) {
      return x -> x == null ? EMPTY : stringifier.apply(x.toString());
    }

    /**
     * Lets you specifiy your own default stringifier, replacing {@link Stringifier#DEFAULT}.
     *
     * @param stringifier The default stringifier to use
     * @return This {@code Builder}
     */
    public Builder setDefaultStringifier(Stringifier stringifier) {
      this.defStringifier = Check.notNull(stringifier).ok();
      return this;
    }

    /**
     * Assigns the specified stringifier to the specified variables within the specified template.
     * The variable names are taken to be fully-qualified names, relative to the specified template.
     *
     * @see TemplateUtils#getFQName(Template, String)
     * @see TemplateUtils#getParentTemplate(Template, String)
     * @param stringifier The stringifier
     * @param template The template containing the variables
     * @param varFQNames The variables
     * @return This {@code Builder}
     */
    public Builder addStringifier(
        Stringifier stringifier, Template template, String... varFQNames) {
      Check.notNull(stringifier, "stringifier");
      Check.notNull(template, "template");
      Check.that(varFQNames, "varFQNames").is(deepNotEmpty());
      for (String var : varFQNames) {
        Template tmpl = TemplateUtils.getParentTemplate(template, var);
        // Make sure var is a variable name, not a nested template name
        Check.that(var).is(in(), tmpl.getVariables());
        StringifierId id = new StringifierId(template, var);
        Check.that(id)
            .isNot(keyIn(), stringifiers, ERR_VAR_ASSIGNED)
            .then(x -> stringifiers.put(x, stringifier));
      }
      return this;
    }

    /**
     * Assigns the specified stringifier to the specified {@link XVarGroup variable groups}. The
     * group that a variable belongs to can be specified as a prefix within the variable
     * declaration. For example in {@code ~%format2:salary%} the {@code salary} variable is assigned
     * to variable group "format2". A variable group can also be assigned via the {@link
     * RenderSession} class. See {@link RenderSession#set(String, Object, XVarGroup)}. Note that
     * different instances of the same variable within the same template can be assigned to
     * different variable groups (for example: {@code ~%html:fullName%} and {@code ~%js:fullName%}).
     *
     * @param stringifier The stringifier
     * @param groupNames The names of the variable groups to which to assign the stringifier
     * @return This {@code Builder}
     */
    public Builder addGroupStringifier(Stringifier stringifier, String... groupNames) {
      Check.notNull(stringifier, "stringifier");
      Check.that(groupNames, "groupNames").is(deepNotEmpty());
      for (String name : groupNames) {
        VarGroup vg = XVarGroup.withName(name);
        StringifierId id = new StringifierId(vg);
        Check.that(id)
            .isNot(keyIn(), stringifiers, ERR_GROUP_ASSIGNED)
            .then(x -> stringifiers.put(x, stringifier));
      }
      return this;
    }

    /**
     * Assigns the specified stringifier to all variables with the specified name, irrespective of
     * which template they belong to. The provided variable names must be simple names (rather than
     * fully-qualified names). They may, however, start and/or end with a '*' sign. For example to
     * assign a number formatter to all variables whose name ends with "Price", specify {@code
     * *Price} as the variable name.
     *
     * @param stringifier The stringifier
     * @param varNames The variable names to associate the stringifier with.
     * @return This {@code Builder}
     */
    public Builder addNameBasedStringifier(Stringifier stringifier, String... varNames) {
      Check.notNull(stringifier, "stringifier");
      Check.that(varNames, "varNames").is(deepNotEmpty());
      for (String var : varNames) {
        if (var.startsWith("*") || var.endsWith("*")) {
          partialNames.add(Tuple.of(var, stringifier));
        } else {
          StringifierId id = new StringifierId(var);
          Check.that(id)
              .isNot(keyIn(), stringifiers, ERR_VAR_ASSIGNED)
              .then(x -> stringifiers.put(x, stringifier));
        }
      }
      return this;
    }

    /**
     * Assigns the specified stringifier to the specified types. In other words, if a value is an
     * instance of one of those types, then it will be stringified using the specified stringifier,
     * whatever the variable receiving that value. Internally, type-based stringifiers are stored
     * into, and looked up in a {@link TypeMap}. This means that if there is no stringifier defined
     * for, say, {@code Short.class}, but there is a stringifier for {@code Number.class}, then that
     * is the stringifier that is going to be used for {@code Short} values. This saves you from
     * having to specify a stringifier for each and every subclass of {@code Number} if they can all
     * be stringified in the same way.
     *
     * @param stringifier The stringifier
     * @param types The types to associate the stringifier with.
     * @return This {@code Builder}
     */
    public Builder addTypeBasedStringifier(Stringifier stringifier, Class<?>... types) {
      Check.notNull(stringifier, "stringifier");
      Check.that(types, "types").is(deepNotEmpty());
      for (Class<?> t : types) {
        Check.that(t)
            .isNot(keyIn(), typeStringifiers, ERR_TYPE_ASSIGNED, t.getName())
            .then(x -> typeStringifiers.put(x, stringifier));
      }
      return this;
    }

    /**
     * Explicitly sets the data type of the specified variables within the specified template.
     * Thisiers enables the {@code StringifierFactory} to find a type-based stringifier for a value
     * even if the value is {@code null} (in which case {@code Object.getClass()} is not available
     * to determine the variable's type).
     *
     * @param type The data type to set for the specified variables
     * @param template The template containing the variables
     * @param varFQNames The fully-qualified name of the variables
     * @return This {@code Builder}
     */
    public Builder setDataType(Class<?> type, Template template, String... varFQNames) {
      Check.notNull(type, "type");
      Check.notNull(template, "template");
      Check.that(varFQNames, "varFQNames").is(deepNotEmpty());
      for (String var : varFQNames) {
        Template tmpl = TemplateUtils.getParentTemplate(template, var);
        // Make sure var is a variable name, not a nested template name
        Check.that(var).is(in(), tmpl.getVariables());
        Tuple<Template, String> tuple = Tuple.of(tmpl, var);
        Check.that(tuple)
            .isNot(keyIn(), typeLookup, ERR_TYPE_SET)
            .then(x -> typeLookup.put(tuple, type));
      }
      return this;
    }

    /**
     * Returns a new, immutable {@code StringifierFactory} instance.
     *
     * @return A new, immutable {@code StringifierFactory} instance
     */
    public StringifierFactory freeze() {
      return new StringifierFactory(
          stringifiers, typeStringifiers, typeLookup, partialNames, defStringifier);
    }
  }

  /* +++++++++++++++++++++[ END BUILDER CLASS ]++++++++++++++++++ */

  /**
   * Returns a {@code Builder} instance that lets you configure the stringifiers for a template.
   *
   * @param template The template for which to define the stringifiers
   * @param globalStringifiers A {@code TypeStringifiers} instance to retrieve type-based
   *     stringifiers from
   * @return A {@code Builder} instance that lets you configure a {@code StringifierFactory}
   *     instance
   */
  public static Builder configure() {
    return new Builder();
  }

  private final Map<StringifierId, Stringifier> stringifiers;
  private final Map<Class<?>, Stringifier> typeStringifiers;
  private final Map<Tuple<Template, String>, Class<?>> typeLookup;
  private final List<Tuple<String, Stringifier>> partialNames;
  private final Stringifier defStringifier;

  private StringifierFactory(
      Map<StringifierId, Stringifier> stringifiers,
      Map<Class<?>, Stringifier> typeStringifiers,
      Map<Tuple<Template, String>, Class<?>> typeLookup,
      List<Tuple<String, Stringifier>> partials,
      Stringifier defStringifier) {
    this.stringifiers = Map.copyOf(stringifiers);
    this.typeStringifiers = new TypeMap<>(typeStringifiers);
    this.partialNames = List.copyOf(partials);
    this.typeLookup = Map.copyOf(typeLookup);
    this.defStringifier = defStringifier;
  }

  Stringifier getStringifier(VariablePart part, VarGroup defaultGroup, Object value)
      throws RenderException {
    StringifierId id;
    Stringifier sf;
    if (part.getVarGroup().isPresent()) {
      VarGroup vg = part.getVarGroup().get();
      id = new StringifierId(vg);
      if (null != (sf = stringifiers.get(id))) {
        return sf;
      }
    } else if (defaultGroup != null) {
      id = new StringifierId(defaultGroup);
      if (null != (sf = stringifiers.get(id))) {
        return sf;
      }
      throw RenderException.noStringifierForGroup(defaultGroup);
    }
    Template tmpl = part.getParentTemplate();
    String var = part.getName();
    id = new StringifierId(tmpl, var);
    if (null != (sf = stringifiers.get(id))) {
      return sf;
    }
    id = new StringifierId(null, var);
    if (null != (sf = stringifiers.get(id))) {
      return sf;
    }
    for (Tuple<String, Stringifier> partial : partialNames) {
      String name = partial.getLeft();
      if (name.startsWith("*")) {
        if (name.endsWith("*") && var.contains(trim(name, "*"))) {
          return partial.getRight();
        } else if (var.endsWith(ltrim(name, "*"))) {
          return partial.getRight();
        }
      } else if (var.startsWith(rtrim(name, "*"))) {
        return partial.getRight();
      }
    }
    Class<?> type = typeLookup.get(Tuple.of(tmpl, var));
    if (type == null && value != null) {
      type = value.getClass();
    }
    if (type != null) {
      if (null != (sf = typeStringifiers.get(type))) {
        return sf;
      }
    }
    return defStringifier;
  }
}
