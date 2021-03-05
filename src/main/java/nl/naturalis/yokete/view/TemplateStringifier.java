package nl.naturalis.yokete.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.naturalis.common.StringMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.notIn;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.check.CommonChecks.nullPointer;

public final class TemplateStringifier {

  public static interface VariableStringifier {
    /**
     * Stringifies the specified value.
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

    private VariableStringifier fbs;
    private ApplicationStringifier asf;
    private Map<Tuple<String, String>, Class<?>> varTypes = new HashMap<>();
    private Map<Tuple<String, String>, VariableStringifier> stringifiers = new HashMap<>();

    public Builder setFallbackStringifier(VariableStringifier stringifier) {
      Check.that(fbs).is(nullPointer(), "Fallback stringifier already set");
      fbs = Check.notNull(stringifier).ok();
      return this;
    }

    public Builder setApplicationStringifier(ApplicationStringifier stringifier) {
      Check.that(asf).is(nullPointer(), "Application stringifier already set");
      asf = Check.notNull(stringifier).ok();
      return this;
    }

    public Builder addStringifier(String varName, VariableStringifier stringifier) {
      return addStringifier(tmpl.getName(), varName, stringifier);
    }

    public Builder addStringifier(
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

    public Builder setType(String varName, Class<?> type) {
      return setType(tmpl.getName(), varName, type);
    }

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

  public static Builder configure(Template template) {
    return new Builder(template);
  }

  private final VariableStringifier fbsf;
  private final Map<Tuple<String, String>, VariableStringifier> stringifiers;
  private final Map<Tuple<String, String>, Class<?>> varTypes;

  private TemplateStringifier(
      VariableStringifier fallbackStringifier,
      Map<Tuple<String, String>, VariableStringifier> stringifiers,
      Map<Tuple<String, String>, Class<?>> varTypes) {
    this.fbsf = fallbackStringifier;
    this.stringifiers = stringifiers;
    this.varTypes = varTypes;
  }

  public void stringify(String tmplName, String varName, Object value) {
    Check.notNull(tmplName, "tmplName");
    Check.notNull(varName, "varName");
    Tuple<String, String> t = Tuple.of(tmplName, varName);
  }
}
