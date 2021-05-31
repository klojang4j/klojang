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
    this.reader = Check.notNull(beanClass, "beanClass").ok(BeanReader::new);
    this.nameMap = null;
  }

  public BeanAccessor(Class<T> beanClass, Template template) {
    this(beanClass, template, NameMapper.NOOP);
  }

  public BeanAccessor(Class<T> beanClass, Template template, NameMapper mapper) {
    Check.notNull(beanClass, "beanClass");
    Check.notNull(template, "template");
    Check.notNull(mapper, "mapper");
    Map<String, String> tmp = new HashMap<>(template.getNames().size());
    template.getNames().forEach(n -> tmp.put(n, mapper.map(template, n)));
    String[] properties = tmp.values().toArray(String[]::new);
    reader = new BeanReader<>(beanClass, properties);
    tmp.keySet().removeAll(reader.getUsedProperties());
    nameMap = Map.copyOf(tmp);
  }

  @Override
  public Object access(T sourceData, String name) throws RenderException {
    Check.notNull(sourceData, "sourceData");
    String key = Check.notNull(name, "name").ok(nameMap::get);
    if (key == null) {
      return UNDEFINED;
    }
    try {
      return reader.read(sourceData, key);
    } catch (NoSuchPropertyException e) {
      return UNDEFINED;
    }
  }
}
