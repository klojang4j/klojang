package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.function.Supplier;
import nl.naturalis.common.invoke.Setter;

class PropertyWriter implements Writer {

  static <U> U toBean(
      ResultSet rs, Supplier<U> beanSupplier, PropertyWriter[] writers, ResultSetReaderConfig cfg)
      throws Throwable {
    U bean = beanSupplier.get();
    for (PropertyWriter writer : writers) {
      Object value =
          writer.synapse.fire(rs, writer.jdbcIdx, (Class<U>) writer.setter.getParamType(), cfg);
      writer.setProperty(bean, value);
    }
    return bean;
  }

  private final Synapse<?, ?> synapse;
  private final Setter setter;
  private final int jdbcIdx;
  private final int sqlType;

  PropertyWriter(Synapse<?, ?> synapse, Setter setter, int jdbcIdx, int sqlType) {
    this.synapse = synapse;
    this.setter = setter;
    this.jdbcIdx = jdbcIdx;
    this.sqlType = sqlType;
  }

  @Override
  public int getSqlType() {
    return sqlType;
  }

  private void setProperty(Object bean, Object value) throws Throwable {
    setter.getMethod().invoke(bean, value);
  }
}
