package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.rs.RsToBeanTransporter;
import static nl.naturalis.common.check.CommonChecks.gt;
import static nl.naturalis.yokete.db.rs.RsToBeanTransporter.toBean;

class DefaultBeanifier<T> implements ResultSetBeanifier<T> {

  private final ResultSet rs;
  private final RsToBeanTransporter<?, ?>[] setters;
  private final Supplier<T> beanSupplier;

  DefaultBeanifier(ResultSet rs, RsToBeanTransporter<?, ?>[] ss, Supplier<T> bs) {
    this.rs = rs;
    this.setters = ss;
    this.beanSupplier = bs;
  }

  @Override
  public Optional<T> beanify() {
    try {
      return Optional.of(toBean(rs, beanSupplier, setters));
    } catch (Throwable e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  @Override
  public List<T> beanifyAtMost(int limit) {
    Check.that(limit, "limit").is(gt(), 0);
    try {
      return atMost(limit);
    } catch (Throwable e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  @Override
  public List<T> beanifyAtMost(int from, int limit) {
    Check.that(limit, "limit").is(gt(), 0);
    try {
      for (int i = 0; i < from; ++i) {
        if (!rs.next()) {
          return Collections.emptyList();
        }
      }
      return atMost(limit);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  public List<T> beanifyAll() {
    return beanifyAll(16);
  }

  @Override
  public List<T> beanifyAll(int sizeEstimate) {
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    List<T> all = new ArrayList<>(sizeEstimate);
    try {
      while (rs.next()) {
        all.add(toBean(rs, beanSupplier, setters));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  private List<T> atMost(int limit) throws Throwable {
    List<T> all = new ArrayList<>(limit);
    int i = 0;
    do {
      all.add(toBean(rs, beanSupplier, setters));
    } while (++i < limit && rs.next());
    return all;
  }

  @Override
  public void close() {
    try {
      rs.close();
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }
}
