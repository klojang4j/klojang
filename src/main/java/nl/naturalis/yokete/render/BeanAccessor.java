package nl.naturalis.yokete.render;

import nl.naturalis.common.invoke.AnyBeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.template.Template;

public class BeanAccessor implements Accessor<Object> {

  private final AnyBeanReader reader = new AnyBeanReader();

  public BeanAccessor() {}

  @Override
  public Object access(Object from, String name) throws RenderException {
    try {
      return reader.read(from, name);
    } catch (NoSuchPropertyException e) {
      return UNDEFINED;
    }
  }

  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return this;
  }
}
