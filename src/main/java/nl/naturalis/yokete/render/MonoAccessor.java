package nl.naturalis.yokete.render;

import nl.naturalis.yokete.template.Template;

/**
 * A brain-dead Accessor implementation used by the fillMono() method.
 *
 * @author Ayco Holleman
 */
class MonoAccessor implements Accessor<Object> {

  MonoAccessor() {}

  @Override
  public Object access(Object sourceData, String name) throws RenderException {
    return sourceData;
  }

  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return null;
  }
}