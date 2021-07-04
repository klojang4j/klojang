package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.Emitter;
import nl.naturalis.yokete.db.rs.EmitterNegotiator;
import static nl.naturalis.common.ObjectMethods.ifNull;

public class SQLQuery extends SQLStatement<SQLQuery> {

  private UnaryOperator<String> nameMapper = UnaryOperator.identity();

  private PreparedStatement ps;
  private ResultSet rs;

  public SQLQuery(Connection con, SQL sql) {
    super(con, sql);
  }

  public SQLQuery withNameMapper(UnaryOperator<String> columnToKeyOrPropertyMapper) {
    this.nameMapper = Check.notNull(columnToKeyOrPropertyMapper).ok();
    return this;
  }

  public ResultSet execute() {
    try {
      return resultSet();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public ResultSet executeAndNext() {
    try {
      ResultSet rs = resultSet();
      if (!rs.next()) {
        throw new KSQLException("Query returned zero rows");
      }
      return rs;
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns the value of the first column of the first row, converted to the specified type. Throws
   * a {@link KSQLException} if the query returned zero rows.
   *
   * @param <T>
   * @param clazz
   * @return
   */
  public <T> T lookup(Class<T> clazz) {
    ResultSet rs = executeAndNext();
    try {
      int sqlType = rs.getMetaData().getColumnType(1);
      Emitter<?, T> emitter = EmitterNegotiator.getInstance().getEmitter(clazz, sqlType);
      return emitter.getValue(rs, 1, clazz);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns the value of the first column of the first row as an integer. Throws a {@link
   * KSQLException} if the query returned zero rows.
   *
   * @return The value of the first column of the first row as an integer
   * @throws KSQLException If the query returned zero rows
   */
  public int getInt() throws KSQLException {
    try {
      return executeAndNext().getInt(1);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  public String getString() throws KSQLException {
    try {
      return executeAndNext().getString(1);
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  public <T> List<T> getList(Class<T> clazz) {
    return getList(clazz, 10);
  }

  public <T> List<T> getList(Class<T> clazz, int expectedSize) {
    try {
      ResultSet rs = resultSet();
      if (!rs.next()) {
        return Collections.emptyList();
      }
      int sqlType = rs.getMetaData().getColumnType(1);
      Emitter<?, T> emitter = EmitterNegotiator.getInstance().getEmitter(clazz, sqlType);
      List<T> list = new ArrayList<>(expectedSize);
      do {
        list.add(emitter.getValue(rs, 1, clazz));
      } while (rs.next());
      return list;
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public Optional<Row> mappify() {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappify();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Row> mappifyAtMost(int limit) {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAtMost(limit);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Row> mappifyAll() {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAll();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Row> mappifyAll(int sizeEstimate) {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAll(sizeEstimate);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public <T> Optional<T> beanify(Class<T> beanClass, Supplier<T> beanSupplier) {
    try {
      BeanifierBox<T> bb = sql.getBeanifierBox(beanClass, beanSupplier, nameMapper);
      return bb.get(resultSet()).beanify();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  public void close() {
    close(ps);
  }

  private PreparedStatement preparedStatement() throws Throwable {
    if (ps == null) {
      ps = con.prepareStatement(sql.getJdbcSQL());
      applyBindings(ps);
    }
    return ps;
  }

  private ResultSet resultSet() throws Throwable {
    try {
      return ifNull(rs, rs = preparedStatement().executeQuery());
    } catch (SQLException e) {
      throw new KSQLException(sql, e);
    }
  }
}
