package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.BeanValueSetter;
import nl.naturalis.yokete.db.rs.MapValueSetter;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static nl.naturalis.common.check.CommonChecks.illegalState;
import static nl.naturalis.common.check.CommonChecks.in;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.db.rs.MapValueSetter.populateMap;

public class SQLInsert extends SQLStatement<SQLInsert> {

  private final Set<Tuple<Object, String>> bindBackObjs = new HashSet<>(4);

  private PreparedStatement ps;

  private Object mruObject;

  public SQLInsert(Connection conn, SQL sql) {
    super(conn, sql);
  }

  /**
   * Binds the values in the specified JavaBean or <code>Map&lt;String,Object&gt;</code> into the
   * {@link PreparedStatement} created to execute the INSERT statement. All properties whose name
   * correspond to one of the named parameters in the {@code SQL} instance passed in through the
   * constructor will be used to populate the {@code PreparedStatement}. Any other properties will
   * be silently ignored. The effect of passing any other than a proper JavaBean or <code>
   * Map&lt;String,Object&gt;</code> (e.g. a {@code Collection}) is undefined.
   *
   * @param beanOrMap The bean or {@code Map} whose values to bind into the {@code
   *     PreparedStatement}
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bind(Object beanOrMap) {
    return super.bind(mruObject = beanOrMap);
  }

  /**
   * Causes the auto-incremented value of the primary key column to be bound back into the bean or
   * {@code Map} specified through the {@code bind} method.
   *
   * @param keyOrProperty The map key or bean property to which to bind the auto-incremented value
   * @return This {@code SQLInsert} instance
   */
  public SQLInsert bindBack(String keyOrProperty) {
    Check.on(illegalState(), mruObject)
        .is(notNull(), "No bean or map to bind back to specified yet");
    Check.notNull(keyOrProperty);
    Tuple<Object, String> tuple = Tuple.of(mruObject, keyOrProperty);
    Check.that(tuple)
        .isNot(in(), bindBackObjs, "No new bean or map to bind back to specified yet")
        .then(bindBackObjs::add);
    return this;
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
              MapValueSetter<?>[] transporters =
                  MapValueSetter.createTransporters(rs, s -> t.getRight());
              populateMap(rs, (Map<String, Object>) t.getLeft(), transporters);
            } else {
              Class<T> beanClass = (Class<T>) t.getLeft().getClass();
              BeanValueSetter<?, ?>[] setters =
                  BeanValueSetter.createSetters(rs, beanClass, s -> t.getRight());
              Supplier<T> beanSupplier = () -> (T) t.getLeft();
              BeanValueSetter.toBean(rs, beanSupplier, setters);
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
          throw new KSQLException("No keys were generated");
        } else if (rs.getMetaData().getColumnCount() != 1) {
          throw new KSQLException("Multiple auto-increment keys not supported");
        }
        return rs.getLong(1);
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /** Closes the {@link PreparedStatement} created for the INSERT statement. */
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
