package nl.naturalis.yokete.render;

import java.util.Map;
import nl.naturalis.common.StringMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.yokete.render.BadStringifierException.applicationStringifierNotNullResistant;
import static nl.naturalis.yokete.render.BadStringifierException.applicationStringifierReturnedNull;
import static nl.naturalis.yokete.render.BadStringifierException.templateStringifierNotNullResistant;
import static nl.naturalis.yokete.render.BadStringifierException.templateStringifierReturnedNull;

class TemplateStringifier implements Stringifier {

  private final Map<Tuple<String, Template>, Stringifier> stringifiers;
  private final Map<Tuple<String, Template>, Class<?>> varTypes;
  private final ApplicationStringifier asf;

  TemplateStringifier(
      Map<Tuple<String, Template>, Stringifier> stringifiers,
      Map<Tuple<String, Template>, Class<?>> varTypes,
      ApplicationStringifier asf) {
    this.stringifiers = Map.copyOf(stringifiers);
    this.varTypes = Map.copyOf(varTypes);
    this.asf = asf;
  }

  @Override
  public String toString(Template template, String varName, Object value) throws RenderException {
    Check.notNull(template, "template");
    Check.notNull(varName, "varName");
    Tuple<String, Template> tuple = Tuple.of(varName, template);
    Stringifier vsf = stringifiers.get(tuple);
    if (vsf != null) {
      try {
        String s = vsf.toString(template, varName, value);
        if (s == null) {
          throw templateStringifierReturnedNull(template, varName);
        }
      } catch (NullPointerException e) {
        throw templateStringifierNotNullResistant(template, varName);
      }
    }
    Class<?> type = varTypes.get(tuple);
    if (type != null) {
      vsf = asf.getStringifier(type);
      try {
        String s = vsf.toString(template, varName, value);
        if (s == null) {
          throw applicationStringifierReturnedNull(type);
        }
      } catch (NullPointerException e) {
        throw applicationStringifierNotNullResistant(type);
      }
    }
    return value == null ? StringMethods.EMPTY : value.toString();
  }
}
