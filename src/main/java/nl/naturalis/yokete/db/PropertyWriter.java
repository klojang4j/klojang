package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.function.Supplier;
import nl.naturalis.common.invoke.Setter;

class PropertyWriter<COLUMN_TYPE, TARGET_TYPE> implements Writer {

  static <U> U toBean(ResultSet rs, Supplier<U> beanSupplier, PropertyWriter<?, ?>[] writers)
      throws Throwable {
    U bean = beanSupplier.get();
    for (PropertyWriter<?, ?> writer : writers) {
      writer.transferValue(rs, bean);
    }
    return bean;
  }

  private final Synapse<COLUMN_TYPE, TARGET_TYPE> synapse;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  PropertyWriter(
      Synapse<COLUMN_TYPE, TARGET_TYPE> synapse, Setter setter, int jdbcIdx, int sqlType) {
    this.synapse = synapse;
    this.setter = setter;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  @SuppressWarnings("unchecked")
  private <U> void transferValue(ResultSet rs, U bean) throws Throwable {
    TARGET_TYPE val = synapse.fire(rs, jdbcIdx, (Class<TARGET_TYPE>) setter.getParamType());
    setter.getMethod().invoke(bean, val);
  }
}
