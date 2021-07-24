package org.klojang.db.ps;

import java.sql.PreparedStatement;
import java.util.function.Function;
import static org.klojang.db.ps.PsSetter.SET_STRING;
import static nl.naturalis.common.ObjectMethods.ifNotEmpty;

/**
 * Binds a single value to a {@link PreparedStatement}, possibly after first converting it to the
 * appropriate type.
 *
 * @author Ayco Holleman
 * @param <FIELD_TYPE> The type of the incoming value, which may originate from a JavaBean field,
 *     but also, for example, from a {@code Map}
 * @param <PARAM_TYPE> The type to which the value is converted before being passed on to one of the
 *     {@code setXXX} methods of {@link PreparedStatement}
 */
class Receiver<FIELD_TYPE, PARAM_TYPE> {

  @SuppressWarnings({"unchecked", "rawtypes"})
  static final Receiver<?, String> ANY_TO_STRING = new Receiver(SET_STRING, String::valueOf);

  private final PsSetter<PARAM_TYPE> setter;
  private final Adapter<FIELD_TYPE, PARAM_TYPE> adapter;

  Receiver(PsSetter<PARAM_TYPE> setter) {
    this.setter = setter;
    this.adapter = null;
  }

  Receiver(PsSetter<PARAM_TYPE> setter, Function<FIELD_TYPE, PARAM_TYPE> adapter) {
    this(setter, (x, y) -> ifNotEmpty(x, adapter::apply));
  }

  Receiver(PsSetter<PARAM_TYPE> setter, Adapter<FIELD_TYPE, PARAM_TYPE> adapter) {
    this.setter = setter;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  PARAM_TYPE getParamValue(FIELD_TYPE beanValue) {
    return adapter == null
        ? (PARAM_TYPE) beanValue
        : adapter.adapt(beanValue, setter.getParamType());
  }

  void bind(PreparedStatement ps, int paramIndex, PARAM_TYPE value) throws Throwable {
    setter.bindValue(ps, paramIndex, value);
  }
}
