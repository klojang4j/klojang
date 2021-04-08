package nl.naturalis.yokete.accessors;

import nl.naturalis.common.invoke.AnyBeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;

/**
 * JavaBeans properties {@code Accessor}. This implementation does not use reflection to read the
 * bean properties, but it still uses reflection to figure out what the properties are. Therefore,
 * if you are using this class within a Java module, you will have to open the module to the Yoketi
 * packages.
 *
 * @author Ayco Holleman
 */
public class AnyBeanAccessor implements Accessor<Object> {

  private final AnyBeanReader reader = new AnyBeanReader();

  public AnyBeanAccessor() {}

  @Override
  public Object access(Object from, String name) throws RenderException {
    try {
      return reader.read(from, name);
    } catch (NoSuchPropertyException e) {
      return UNDEFINED;
    }
  }
}
