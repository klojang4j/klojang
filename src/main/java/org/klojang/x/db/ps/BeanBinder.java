package org.klojang.x.db.ps;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.klojang.db.BindInfo;
import org.klojang.db.NamedParameter;
import org.klojang.db.SQL;
import nl.naturalis.common.ModulePrivate;

/**
 * Binds the values within in a JavaBean to a {@link PreparedStatement}. The {@code
 * PreparedStatement} is presumed to be created from SQL that uses named parameters as a
 * parametrization mechanism. Bean properties are mapped as-is to the named parameters within the
 * SQL. The JavaBean is not required to provide a value for each and every named parameter. The
 * {@link #getBoundParameters()} returns a list of the parameters that could be bound using the bean
 * passed to {@link #bindBean(PreparedStatement, Object)}.
 *
 * @author Ayco Holleman
 * @param <T> The type of the JavaBean
 */
@ModulePrivate
public class BeanBinder<T> {

  private final List<NamedParameter> bound = new ArrayList<>();

  private final BeanValueBinder<?, ?>[] binders;

  /**
   * Creates a {@code BeanBinder} capable binding a JavaBean to the specified query parameters. See
   * {@link SQL#getParameters()}.
   *
   * @param beanClass The type of the JavaBean that can be bound using this {@code BeanBinder}
   * @param params The named parameters that need to be bound
   * @param bindInfo A {@link BindInfo} object that allows you to override or fine-tune the default
   *     binding behaviour.
   */
  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, BindInfo bindInfo) {
    binders = BeanValueBinder.createBeanValueBinders(beanClass, params, bindInfo, bound);
  }

  /**
   * Binds the values contained in the specified bean to the specified {@code PreparedStatement}.
   *
   * @param ps The {@code PreparedStatement}
   * @param bean The JavaBean
   * @throws Throwable
   */
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
