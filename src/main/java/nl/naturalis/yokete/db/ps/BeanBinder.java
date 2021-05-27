package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.yokete.db.BindInfo;
import nl.naturalis.yokete.db.NamedParameter;

@ModulePrivate
public class BeanBinder<T> {

  private final List<NamedParameter> bound = new ArrayList<>();

  private final BeanValueBinder<?, ?>[] binders;

  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, BindInfo bindInfo) {
    binders = BeanValueBinder.createBeanValueBinders(beanClass, params, bindInfo, bound);
  }

  public void bindBean(PreparedStatement ps, T bean) throws Throwable {
    BeanValueBinder.bindBean(ps, bean, binders);
  }

  /**
   * Returns the parameters in the query string that will be bound by this {@code BeanBinder}.
   *
   * @return The parameters in the query string that will be bound by this {@code BeanBinder}.
   */
  public List<NamedParameter> getBoundParameters() {
    return List.copyOf(bound);
  }
}
