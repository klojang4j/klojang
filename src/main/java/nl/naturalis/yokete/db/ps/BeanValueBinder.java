package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.ClassMethods;
import nl.naturalis.common.invoke.Getter;
import nl.naturalis.common.invoke.GetterFactory;
import nl.naturalis.yokete.db.BindInfo;
import nl.naturalis.yokete.db.NamedParameter;

/* Binds a single value from a JavaBean into a PreparedStatement */
class BeanValueBinder<FIELD_TYPE, PARAM_TYPE> {

  static <T> void bindBean(PreparedStatement ps, T bean, BeanValueBinder<?, ?>[] binders)
      throws Throwable {
    for (BeanValueBinder<?, ?> binder : binders) {
      binder.bindValue(ps, bean);
    }
  }

  static BeanValueBinder<?, ?>[] createBeanValueBinders(
      Class<?> beanClass,
      List<NamedParameter> params,
      BindInfo bindInfo,
      Collection<NamedParameter> bound) {
    ReceiverNegotiator negotiator = ReceiverNegotiator.getInstance();
    Map<String, Getter> getters = GetterFactory.INSTANCE.getGetters(beanClass, true);
    List<BeanValueBinder<?, ?>> vts = new ArrayList<>(params.size());
    for (NamedParameter param : params) {
      Getter getter = getters.get(param.getName());
      if (getter == null) {
        continue;
      }
      bound.add(param);
      String property = param.getName();
      Class<?> type = getter.getReturnType();
      Receiver<?, ?> receiver;
      if (ClassMethods.isA(type, Enum.class) && bindInfo.saveEnumUsingToString(property)) {
        receiver = EnumReceivers.ENUM_TO_STRING;
      } else {
        Integer sqlType = bindInfo.getSqlType(property, type);
        if (sqlType == null) {
          receiver = negotiator.getDefaultReceiver(type);
        } else {
          receiver = negotiator.getReceiver(type, sqlType);
        }
      }
      vts.add(new BeanValueBinder<>(getter, receiver, param));
    }
    return vts.toArray(BeanValueBinder[]::new);
  }

  private final Getter getter;
  private final Receiver<FIELD_TYPE, PARAM_TYPE> receiver;
  private final NamedParameter param;

  private BeanValueBinder(
      Getter getter, Receiver<FIELD_TYPE, PARAM_TYPE> receiver, NamedParameter param) {
    this.getter = getter;
    this.receiver = receiver;
    this.param = param;
  }

  private <T> void bindValue(PreparedStatement ps, T bean) throws Throwable {
    FIELD_TYPE beanValue = (FIELD_TYPE) getter.getMethod().invoke(bean);
    PARAM_TYPE paramValue = receiver.getParamValue(beanValue);
    System.out.printf("Processing named parameter %s%n", param.getName());
    param.getIndices().forEachThrowing(i -> receiver.bind(ps, i, paramValue));
  }
}
