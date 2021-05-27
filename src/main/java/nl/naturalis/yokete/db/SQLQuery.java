package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.ObjectMethods.ifNull;
import static nl.naturalis.common.check.CommonChecks.yes;

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

  public List<Row> mappifyAtMost(int limit) {
    try {
      bind(preparedStatement());
      MappifierBox mb = sql.getMappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAtMost(limit);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public List<Row> mappifyAll(int sizeEstimate) {
    try {
      bind(preparedStatement());
      MappifierBox mb = sql.getMappifierBox(nameMapper);
      return mb.get(resultSet()).mappifyAll(sizeEstimate);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  public <T> Optional<T> beanify(Class<T> beanClass, Supplier<T> beanSupplier) {
    try {
      bind(preparedStatement());
      BeanifierBox<T> bb = sql.getBeanifierBox(beanClass, beanSupplier, nameMapper);
      return bb.get(resultSet()).beanify();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  public void close() {
    if (rs != null) {
      try {
        if (!rs.isClosed()) {
          rs.close();
        }
      } catch (SQLException e) {
        throw ExceptionMethods.uncheck(e);
      }
    }
    close(ps);
  }

  private PreparedStatement preparedStatement() throws SQLException {
    return ifNull(ps, ps = con.prepareStatement(sql.getNormalizedSQL()));
  }

  private ResultSet resultSet() {
    Check.on(s -> notExecutable(), isExecutable()).is(yes());
    try {
      return ifNull(rs, rs = ps.executeQuery());
    } catch (SQLException e) {
      throw new KSQLException(sql, e);
    }
  }
}
