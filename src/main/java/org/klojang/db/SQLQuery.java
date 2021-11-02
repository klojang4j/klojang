package org.klojang.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.klojang.render.NameMapper;
import org.klojang.x.db.rs.ExtractorNegotiator;
import org.klojang.x.db.rs.RsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.check.Check;

public class SQLQuery extends SQLStatement<SQLQuery> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLQuery.class);

  private NameMapper nameMapper = NameMapper.NOOP;

  private PreparedStatement ps;
  private ResultSet rs;

  public SQLQuery(Connection con, SQL sql) {
    super(con, sql);
  }

  public SQLQuery withNameMapper(NameMapper columnToKeyOrPropertyMapper) {
    this.nameMapper = Check.notNull(columnToKeyOrPropertyMapper).ok();
    return this;
  }

  public ResultSet execute() {
    try {
      return resultSet();
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public ResultSet executeAndNext() {
    try {
      ResultSet rs = resultSet();
      if (!rs.next()) {
        throw new KJSQLException("Query returned zero rows");
      }
      return rs;
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  /**
   * Returns the value of the first column of the first row, converted to the specified type. Throws
   * a {@link KJSQLException} if the query returned zero rows.
   *
   * @param <T>
   * @param clazz
   * @return
   */
  public <T> T lookup(Class<T> clazz) {
    ResultSet rs = executeAndNext();
    try {
      int sqlType = rs.getMetaData().getColumnType(1);
      RsExtractor<?, T> emitter = ExtractorNegotiator.getInstance().findExtractor(clazz, sqlType);
      return emitter.getValue(rs, 1, clazz);
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  /**
   * Returns the value of the first column of the first row as an integer. Throws a {@link
   * KJSQLException} if the query returned zero rows.
   *
   * @return The value of the first column of the first row as an integer
   * @throws KJSQLException If the query returned zero rows
   */
  public int getInt() throws KJSQLException {
    try {
      return executeAndNext().getInt(1);
    } catch (SQLException e) {
      throw KJSQLException.wrap(e, sql);
    }
  }

  /**
   * Returns the value of the first column of the first row as a {@code String}. Throws a {@link
   * KJSQLException} if the query returned zero rows.
   *
   * @return The value of the first column of the first row as aa {@code String}
   * @throws KJSQLException If the query returned zero rows
   */
  public String getString() throws KJSQLException {
    try {
      return executeAndNext().getString(1);
    } catch (SQLException e) {
      throw KJSQLException.wrap(e, sql);
    }
  }

  /**
   * Returns a {@code List} of the values of the first column in the rows selected by the query.
   * Equivalent to {@code getList(clazz, 10)}.
   *
   * @param <T> The desired type of the values
   * @param clazz The desired class of the values
   * @return A {@code List} of the values of the first column in the rows selected by the query
   */
  public <T> List<T> getList(Class<T> clazz) {
    return getList(clazz, 10);
  }

  /**
   * Returns a {@code List} of the values of the first column in the rows selected by the query.
   *
   * @param <T> The desired type of the values
   * @param clazz The desired class of the values
   * @param expectedSize The expected number of rows
   * @return A {@code List} of the values of the first column in the rows selected by the query
   */
  public <T> List<T> getList(Class<T> clazz, int expectedSize) {
    try {
      ResultSet rs = resultSet();
      if (!rs.next()) {
        return Collections.emptyList();
      }
      int sqlType = rs.getMetaData().getColumnType(1);
      RsExtractor<?, T> extractor = ExtractorNegotiator.getInstance().findExtractor(clazz, sqlType);
      List<T> list = new ArrayList<>(expectedSize);
      do {
        list.add(extractor.getValue(rs, 1, clazz));
      } while (rs.next());
      return list;
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public Optional<Row> mappify() {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappify();
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public List<Row> mappifyAtMost(int limit) {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAtMost(limit);
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public List<Row> mappifyAll() {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAll();
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public List<Row> mappifyAll(int sizeEstimate) {
    try {
      MappifierBox mb = new MappifierBox(nameMapper);
      List<Row> rows = mb.get(resultSet()).mappifyAll(sizeEstimate);
      LOG.trace("Query returned {} record(s)", rows.size());
      return rows;
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public <T> Optional<T> beanify(Class<T> beanClass, Supplier<T> beanSupplier) {
    try {
      BeanifierFactory<T> bb = sql.getBeanifierBox(beanClass, beanSupplier, nameMapper);
      return bb.getBeanifier(resultSet()).beanify();
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  public <T> List<T> beanifyAll(Class<T> beanClass, Supplier<T> beanSupplier) {
    try {
      BeanifierFactory<T> bb = sql.getBeanifierBox(beanClass, beanSupplier, nameMapper);
      List<T> beans = bb.getBeanifier(resultSet()).beanifyAll();
      LOG.trace("Query returned {} record(s)", beans.size());
      return beans;
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, sql);
    }
  }

  @Override
  public void close() {
    close(ps);
  }

  private ResultSet resultSet() throws Throwable {
    if (rs == null) {
      LOG.trace("Executing query");
      rs = preparedStatement().executeQuery();
    }
    return rs;
  }

  private PreparedStatement preparedStatement() throws Throwable {
    if (ps == null) {
      ps = con.prepareStatement(sql.getJdbcSQL());
      applyBindings(ps);
    }
    return ps;
  }
}
