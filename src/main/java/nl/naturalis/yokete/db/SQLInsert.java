package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SQLInsert extends SQLStatement {

  private PreparedStatement ps;
  private boolean bindBack;
  private UnaryOperator<String> mapper = UnaryOperator.identity();

  public SQLInsert(Connection conn, SQL sql) {
    super(conn, sql);
  }

  @Override
  public SQLInsert bind(Object bean) {
    return (SQLInsert) super.bind(bean);
  }

  @Override
  public SQLInsert bind(Map<String, Object> map) {
    return (SQLInsert) super.bind(map);
  }

  @Override
  public SQLInsert bind(String param, Object value) {
    return (SQLInsert) super.bind(param, value);
  }

  /**
   * Causes auto-increment keys to be bound back into the JavaBean or {@code Map}. Hence, in case of
   * a {@code Map}, make sure it is modifiable.
   *
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bindBack() {
    this.bindBack = true;
    return this;
  }

  /**
   * Sets the column-to-property or column-to-key mapper to use when binding auto-increment keys
   * back into the JavaBean or {@code Map}, or when calling {@link #executeAndReturnKeys()}. By
   * default a one-to-one mapping is assumed.
   *
   * @param columnToKeyOrPropertyMapper
   * @return
   */
  public SQLInsert withMapper(UnaryOperator<String> columnToKeyOrPropertyMapper) {
    this.mapper = columnToKeyOrPropertyMapper;
    return this;
  }

  public void execute() {
    try {
      exec(bindBack);
      if (bindBack) {
        try (ResultSet rs = ps.getGeneratedKeys()) {
          bindBack(rs);
        }
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public Row executeAndReturnKeys() {
    try {
      exec(true);
      try (ResultSet rs = ps.getGeneratedKeys()) {
        Row keys = getKeys(rs);
        if (bindBack) {
          bindBack(rs, keys);
        }
        return keys;
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public int executeAndReturnIntKey() {
    Row row = executeAndReturnKeys();
    if (row.getColumnCount() != 1) {
      throw new ResultSetReadException("Cannot return int value for compound key");
    }
    return row.getInt(0);
  }

  @Override
  public void close() {
    close(ps);
  }

  private void exec(boolean returnKeys) throws Throwable {
    int keys = returnKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
    ps = con.prepareStatement(sql.getNormalizedSQL(), keys);
    bind(ps);
    ps.executeUpdate();
  }

  @SuppressWarnings("unchecked")
  private void bindBack(ResultSet rs, Row keys) throws Throwable {
    TransporterCache tc = TransporterCache.INSTANCE;
    for (Object obj : bindables) {
      if (obj instanceof Map) {
        ((Map<String, Object>) obj).putAll(keys.toMap());
      } else {
        BeanValueTransporter<?, ?>[] bvt = tc.getBeanValueTransporters(rs, obj.getClass(), mapper);
        BeanValueTransporter.toBean(rs, () -> obj, bvt);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void bindBack(ResultSet rs) throws Throwable {
    TransporterCache tc = TransporterCache.INSTANCE;
    Row keys = null;
    for (Object obj : bindables) {
      if (obj instanceof Map) {
        if (keys == null) {
          keys = getKeys(rs);
        }
        ((Map<String, Object>) obj).putAll(keys.toMap());
      } else {
        BeanValueTransporter<?, ?>[] bvt = tc.getBeanValueTransporters(rs, obj.getClass(), mapper);
        BeanValueTransporter.toBean(rs, () -> obj, bvt);
      }
    }
  }

  private Row getKeys(ResultSet rs) throws Throwable {
    TransporterCache tc = TransporterCache.INSTANCE;
    MapValueTransporter<?>[] mvt = tc.getMapValueTransporters(rs, mapper);
    return MapValueTransporter.toRow(rs, mvt);
  }
}
