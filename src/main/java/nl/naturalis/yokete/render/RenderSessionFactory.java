package nl.naturalis.yokete.render;

import java.util.ArrayList;
import java.util.List;
import nl.naturalis.common.CollectionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;

public final class RenderSessionFactory {

  final Template template;
  final Accessor accessor;
  final TemplateStringifier stringifier;

  public RenderSessionFactory(
      Template template, Accessor accessor, TemplateStringifier stringifier) {
    this.template = Check.notNull(template).ok();
    this.accessor = Check.notNull(accessor).ok();
    this.stringifier = Check.notNull(stringifier).ok();
  }

  public RenderSession newRenderSession() {
    return new RenderSession(this);
  }

  RenderSession newChildSession(String nestedTemplateName) {
    Template nested = template.getNestedTemplate(nestedTemplateName);
    Accessor acc = accessor.getAccessorForTemplate(nested);
    RenderSessionFactory rsf = new RenderSessionFactory(nested, acc, stringifier);
    return new RenderSession(rsf);
  }

  List<String> stringify(Object data, String varName) throws RenderException {
    Object value = accessor.access(data, varName);
    List<?> values = CollectionMethods.asList(value);
    List<String> strvals = new ArrayList<>(values.size());
    for (Object val : values) {
      String strval = stringifier.stringify(template, varName, val);
      strvals.add(strval);
    }
    return strvals;
  }

  Template getTemplate() {
    return template;
  }

  Accessor getAccessor() {
    return accessor;
  }

  TemplateStringifier getStringifier() {
    return stringifier;
  }
}
