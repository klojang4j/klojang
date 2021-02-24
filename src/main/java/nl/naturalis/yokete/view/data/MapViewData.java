package nl.naturalis.yokete.view.data;

import java.util.Map;
import java.util.Optional;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.view.RenderException;
import nl.naturalis.yokete.view.ViewData;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.view.RenderException.nestedMapExpected;

public class MapViewData extends AbstractViewData {

  private Map<String, Object> data;

  public MapViewData(ViewDataStringifiers stringifiers) {
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
