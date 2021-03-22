package nl.naturalis.yokete.render;

import nl.naturalis.common.Pair;
import nl.naturalis.yokete.template.Template;

/**
 * An (almost) brain-dead Accessor implementation used by the fillDuo() method.
 *
 * @author Ayco Holleman
 */
class DuoAccessor implements Accessor<Pair<Object>> {

  private String var0;

  DuoAccessor(String var0) {
    this.var0 = var0;
  }

  @Override
  public Object access(Pair<Object> sourceData, String name) throws RenderException {
    if (name.equals(var0)) {
      return sourceData.getFirst();
    }
    return sourceData.getSecond();
  }

  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return null;
  }
}
