package nl.naturalis.yokete.view.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.NoSuchPropertyException;
import nl.naturalis.yokete.view.Template;
import nl.naturalis.yokete.view.ViewData;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.notNull;

public class BeanViewData extends AbstractViewData {

  @SuppressWarnings("rawtypes")
  private static final Map<Tuple<Template, Class>, BeanReader<?>> brCache = new HashMap<>();

  private Object bean;

  public BeanViewData(ViewDataStringifiers stringifiers) {
    super(stringifiers);
  }

  public BeanViewData with(Object bean) {
    this.bean = bean;
    return this;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected Optional<?> getRawValue(Template template, String name) {
    Check.that(bean).is(notNull(), "No data");
    Tuple<Template, Class> key = Tuple.of(template, bean.getClass());
    BeanReader br = brCache.computeIfAbsent(key, this::createBeanReader);
    try {
      return ifNotNull(br.read(bean, name), Optional::of, NULL);
    } catch (NoSuchPropertyException e) {
      return Optional.empty();
    }
  }

  @Override
  protected ViewData createViewData(Template template, Object bean) {
    return new BeanViewData(stringifiers).with(bean);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private BeanReader createBeanReader(Tuple<Template, Class> key) {
    Template template = key.getLeft();
    Class clazz = key.getRight().getClass();
    String[] props = template.getAllNames().stream().toArray(String[]::new);
    return new BeanReader(clazz, props);
  }
}
