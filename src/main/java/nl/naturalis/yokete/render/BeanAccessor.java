package nl.naturalis.yokete.render;

import nl.naturalis.common.invoke.AnyBeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.template.Template;

public class BeanAccessor implements Accessor {

  private final AnyBeanReader reader = new AnyBeanReader();

  public BeanAccessor() {}

  @Override
  public Object access(Object from, String varName) throws RenderException {
    try {
      return reader.read(from, varName);
    } catch (NoSuchPropertyException e) {
      return ABSENT;
    }
  }

  @Override
  public Accessor getAccessorForTemplate(Template nested) {
    return this;
  }
}
