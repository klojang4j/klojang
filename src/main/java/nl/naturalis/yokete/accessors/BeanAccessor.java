package nl.naturalis.yokete.accessors;

import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.NameMapper;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.template.Template;

public class BeanAccessor<T> implements Accessor<T> {

  private final BeanReader<T> reader;
  private final Map<String, String> nameMap;

  public BeanAccessor(Class<T> beanClass) {
    reader = Check.notNull(beanClass, "beanClass").ok(BeanReader::new);
    nameMap = null;
  }

  public BeanAccessor(Class<T> beanClass, Template template) {
    Check.notNull(beanClass, "beanClass");
    Check.notNull(template, "template");
    reader = new BeanReader<>(beanClass, template.getVariables().toArray(String[]::new));
    nameMap = null;
  }

  public BeanAccessor(Class<T> beanClass, Template template, NameMapper mapper) {
    Check.notNull(beanClass, "beanClass");
    Check.notNull(template, "template");
    Check.notNull(mapper, "mapper");
    Map<String, String> tmp = new HashMap<>(template.getVariables().size());
    template.getVariables().forEach(n -> tmp.put(n, mapper.map(template, n)));
    String[] properties = tmp.values().toArray(String[]::new);
    reader = new BeanReader<>(beanClass, properties);
    tmp.keySet().removeAll(reader.getUsedProperties());
    nameMap = Map.copyOf(tmp);
  }

  @Override
  public Object access(T sourceData, String name) throws RenderException {
    Check.notNull(sourceData, "sourceData");
    Check.notNull(name, "name");
    try {
      if (nameMap == null) {
        return reader.read(sourceData, name);
      }
      String property = nameMap.get(name);
      if (property == null) {
        return UNDEFINED;
      }
      return reader.read(sourceData, property);
    } catch (NoSuchPropertyException e) {
      return UNDEFINED;
    }
  }
}
