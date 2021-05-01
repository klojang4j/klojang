package nl.naturalis.yokete.db.write;

import java.sql.PreparedStatement;
import java.util.function.Function;

/**
 * Binds a single value to a {@link PreparedStatement}.
 *
 * @author Ayco Holleman
 * @param <FIELD_TYPE> The type of the incoming value, which may come from a field within a
 *     JavaBean, but also (for example) from a value within a {@code Map}
 * @param <PARAM_TYPE> The type to which the value is converted before being passed on to one of the
 *     {@code setXXX} methods of {@link PreparedStatement}
 */
public class ValuePersister<FIELD_TYPE, PARAM_TYPE> {

  private final ParamWriter<PARAM_TYPE> writer;
  private final Adapter<FIELD_TYPE, PARAM_TYPE> adapter;

  ValuePersister(ParamWriter<PARAM_TYPE> writer) {
    this.writer = writer;
    this.adapter = null;
  }

  ValuePersister(ParamWriter<PARAM_TYPE> writer, Function<FIELD_TYPE, PARAM_TYPE> adapter) {
    this(writer, (x, y) -> adapter.apply(x));
  }

  ValuePersister(ParamWriter<PARAM_TYPE> writer, Adapter<FIELD_TYPE, PARAM_TYPE> adapter) {
    this.writer = writer;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  public PARAM_TYPE getParamValue(FIELD_TYPE beanValue) {
    return adapter == null
        ? (PARAM_TYPE) beanValue
        : adapter.adapt(beanValue, writer.getParamType());
  }

  public void bindValue(PreparedStatement ps, int paramIndex, PARAM_TYPE value) throws Throwable {
    writer.bindValue(ps, paramIndex, value);
  }
}
