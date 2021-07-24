package org.klojang.db.ps;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.klojang.db.BindInfo;
import org.klojang.db.NamedParameter;
import nl.naturalis.common.ModulePrivate;

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
