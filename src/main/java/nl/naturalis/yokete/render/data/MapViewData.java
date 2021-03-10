package nl.naturalis.yokete.render.data;

import java.util.Map;
import java.util.Optional;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.TemplateStringifier;
import nl.naturalis.yokete.render.ViewData;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.render.RenderException.nestedMapExpected;

public class MapViewData extends AbstractViewData {

  private Map<String, Object> data;

  public MapViewData(TemplateStringifier stringifiers) {
    super(stringifiers);
  }

  public MapViewData with(Map<String, Object> data) {
    this.data = data;
    return this;
  }

  @Override
  protected Optional<?> getRawValue(Template template, String name) {
    Check.that(data).is(notNull(), "No data");
    if (data.containsKey(name)) {
      return ifNotNull(data.get(name), Optional::of, NULL);
    }
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ViewData createViewData(Template template, Object data) throws RenderException {
    try {
      return new MapViewData(stringifiers).with((Map<String, Object>) data);
    } catch (ClassCastException e) {
      throw nestedMapExpected(template.getName(), data);
    }
  }
}
