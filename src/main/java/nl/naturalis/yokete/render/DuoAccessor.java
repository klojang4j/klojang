package nl.naturalis.yokete.render;

import nl.naturalis.common.Pair;
import nl.naturalis.yokete.template.Template;

/**
 * An (almost) brain-dead Accessor implementation used by the fillDuo() method.
 *
 * @author Ayco Holleman
 */
class DuoAccessor implements Accessor {

  private String var0;

  DuoAccessor(String var0) {
    this.var0 = var0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object access(Object sourceData, String name) throws RenderException {
    if (name.equals(var0)) {
      return ((Pair<Object>) sourceData).getFirst();
    }
    return ((Pair<Object>) sourceData).getSecond();
  }

  @Override
  public Accessor getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return null;
  }
}
