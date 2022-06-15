package org.klojang.x.db.ps;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.klojang.db.BindInfo;
import org.klojang.db.NamedParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.ClassMethods;
import nl.naturalis.common.invoke.Getter;
import nl.naturalis.common.invoke.GetterFactory;

/**
 * Binds a single value from a JavaBean into a PreparedStatement.
 *
 * @param <FIELD_TYPE>
 * @param <PARAM_TYPE>
 * @author Ayco Holleman
 */
class BeanValueBinder<FIELD_TYPE, PARAM_TYPE> {

  private static final Logger LOG = LoggerFactory.getLogger(BeanValueBinder.class);

  static <T> void bindBean(PreparedStatement ps,
      T bean,
      BeanValueBinder<?, ?>[] binders) throws Throwable {
    LOG.debug("Binding {} to PreparedStatement", bean.getClass().getSimpleName());
    for (BeanValueBinder<?, ?> binder : binders) {
      binder.bindValue(ps, bean);
    }
  }

  static BeanValueBinder<?, ?>[] createBeanValueBinders(Class<?> beanClass,
      List<NamedParameter> params,
      BindInfo bindInfo,
      Collection<NamedParameter> bound) {
    ReceiverNegotiator negotiator = ReceiverNegotiator.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<BeanValueBinder<?, ?>> binders = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.getName());
      if (getter == null) {
        continue;
      }
      bound.add(param);
      String property = param.getName();
      Class<?> type = getter.getReturnType();
      Receiver<?, ?> receiver;
      if (ClassMethods.isSubtype(type, Enum.class)
          && bindInfo.bindEnumUsingToString(property)) {
        receiver = EnumReceivers.ENUM_TO_STRING;
      } else {
        Integer sqlType = bindInfo.getSqlType(property, type);
        if (sqlType == null) {
          receiver = negotiator.getDefaultReceiver(type);
        } else {
          receiver = negotiator.findReceiver(type, sqlType);
        }
      }
      binders.add(new BeanValueBinder<>(getter, receiver, param));
    }
    return binders.toArray(BeanValueBinder[]::new);
  }

  private final Getter getter;
  private final Receiver<FIELD_TYPE, PARAM_TYPE> receiver;
  private final NamedParameter param;

  private BeanValueBinder(Getter getter,
      Receiver<FIELD_TYPE, PARAM_TYPE> receiver,
      NamedParameter param) {
    this.getter = getter;
    this.receiver = receiver;
    this.param = param;
  }

  @SuppressWarnings("unchecked")
  private <T> void bindValue(PreparedStatement ps, T bean) throws Throwable {
    FIELD_TYPE beanValue = (FIELD_TYPE) getter.read(bean);
    PARAM_TYPE paramValue = receiver.getParamValue(beanValue);
    if (beanValue == paramValue) { // No adapter defined
      LOG.debug("-> Parameter \"{}\": {}", getter.getProperty(), paramValue);
    } else {
      String fmt = "-> Parameter \"{}\": {} (bean value: {})";
      LOG.debug(fmt, param.getName(), paramValue, beanValue);
    }
    param.getIndices().forEachThrowing(i -> receiver.bind(ps, i, paramValue));
  }

}
