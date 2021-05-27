package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.util.function.Function;
import static nl.naturalis.common.ObjectMethods.ifNotEmpty;
import static nl.naturalis.yokete.db.ps.PSSetter.SET_STRING;

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

  private final PSSetter<PARAM_TYPE> setter;
  private final Adapter<FIELD_TYPE, PARAM_TYPE> adapter;

  Receiver(PSSetter<PARAM_TYPE> setter) {
    this.setter = setter;
    this.adapter = null;
  }

  Receiver(PSSetter<PARAM_TYPE> setter, Function<FIELD_TYPE, PARAM_TYPE> adapter) {
    this(setter, (x, y) -> ifNotEmpty(x, adapter::apply));
  }

  Receiver(PSSetter<PARAM_TYPE> setter, Adapter<FIELD_TYPE, PARAM_TYPE> adapter) {
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
    System.out.printf("Binding %s to parameter %d using %s%n", value, paramIndex, setter.getName());
    setter.bindValue(ps, paramIndex, value);
  }
}
