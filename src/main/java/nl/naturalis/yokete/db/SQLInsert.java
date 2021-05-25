package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static nl.naturalis.yokete.db.MapValueTransporter.populateMap;

public class SQLInsert extends SQLStatement {

  private final List<Tuple<Object, String>> bindBackObjs = new ArrayList<>(4);

  private PreparedStatement ps;

  public SQLInsert(Connection conn, SQL sql) {
    super(conn, sql);
  }

  /**
   * Binds the values in the specified bean into the {@link PreparedStatement} created for the SQL
   * query.
   *
   * @param bean The bean whose properties to bind into the {@code PreparedStatement}
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bind(Object bean) {
    return (SQLInsert) super.bind(bean);
  }

  /**
   * Binds the values in the specified bean into the {@link PreparedStatement} created for the SQL
   * query. This method will cause the (supposedly auto-generated) value of the primary key column
   * to be bound back to the specified property.
   *
   * @param bean
   * @param idField
   * @return
   */
  public SQLInsert bind(Object bean, String idProperty) {
    Check.notNull(idProperty, "idProperty");
    super.bind(bean);
    bindBackObjs.add(Tuple.of(bean, idProperty));
    return this;
  }

  @Override
  public SQLInsert bind(Map<String, Object> map) {
    return (SQLInsert) super.bind(map);
  }

  public SQLInsert bind(Map<String, Object> map, String idKey) {
    Check.notNull(idKey, "idKey");
    super.bind(map);
    bindBackObjs.add(Tuple.of(map, idKey));
    return this;
  }

  @Override
  public SQLInsert bind(String param, Object value) {
    return (SQLInsert) super.bind(param, value);
  }

  @SuppressWarnings("unchecked")
  public <T> void execute() {
    try {
      if (bindBackObjs.isEmpty()) {
        exec(false);
      } else {
        exec(true);
        try (ResultSet rs = ps.getGeneratedKeys()) {
          if (!rs.next()) {
            throw new KSQLException("No keys were generated during INSERT");
          } else if (rs.getMetaData().getColumnCount() != 1) {
            throw new KSQLException("Multiple auto-increment keys not supported");
          }
          for (Tuple<Object, String> t : bindBackObjs) {
            if (t.getLeft() instanceof Map) {
              MapValueTransporter<?>[] transporters =
                  MapValueTransporter.createTransporters(rs, s -> t.getRight());
              populateMap(rs, (Map<String, Object>) t.getLeft(), transporters);
            } else {
              Class<T> beanClass = (Class<T>) t.getLeft().getClass();
              BeanValueTransporter<?, ?>[] transporters =
                  BeanValueTransporter.createTransporters(rs, beanClass, s -> t.getRight());
              Supplier<T> beanSupplier = () -> (T) t.getLeft();
              BeanValueTransporter.toBean(rs, beanSupplier, transporters);
            }
          }
        }
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public long executeAndGetId() {
    try {
      exec(true);
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (!rs.next()) {
          throw new KSQLException("No keys were generated during INSERT");
        } else if (rs.getMetaData().getColumnCount() != 1) {
          throw new KSQLException("Multiple auto-increment keys not supported");
        }
        return rs.getLong(1);
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
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
}
