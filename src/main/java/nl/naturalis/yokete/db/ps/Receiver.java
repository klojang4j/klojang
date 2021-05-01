package nl.naturalis.yokete.db.ps;

import java.sql.PreparedStatement;
import java.util.function.Function;

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
public class Receiver<FIELD_TYPE, PARAM_TYPE> {

  private final PSSetter<PARAM_TYPE> writer;
  private final Adapter<FIELD_TYPE, PARAM_TYPE> adapter;

  Receiver(PSSetter<PARAM_TYPE> writer) {
    this.writer = writer;
    this.adapter = null;
  }

  Receiver(PSSetter<PARAM_TYPE> writer, Function<FIELD_TYPE, PARAM_TYPE> adapter) {
    this(writer, (x, y) -> adapter.apply(x));
  }

  Receiver(PSSetter<PARAM_TYPE> writer, Adapter<FIELD_TYPE, PARAM_TYPE> adapter) {
    this.writer = writer;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  public PARAM_TYPE getParamValue(FIELD_TYPE beanValue) {
    return adapter == null
        ? (PARAM_TYPE) beanValue
        : adapter.adapt(beanValue, writer.getParamType());
  }

  public void bind(PreparedStatement ps, int paramIndex, PARAM_TYPE value) throws Throwable {
    writer.bindValue(ps, paramIndex, value);
  }
}
