package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gt;
import static nl.naturalis.yokete.db.PropertyWriter.toBean;

public class ResultSetBeanifier<T> {

  final PropertyWriter<?, ?>[] writers;

  private final Supplier<T> beanSupplier;

  ResultSetBeanifier(PropertyWriter<?, ?>[] writers, Supplier<T> beanSupplier) {
    this.beanSupplier = beanSupplier;
    this.writers = writers;
  }

  public Optional<T> beanify(ResultSet rs) {
    Check.notNull(rs);
    try {
      return Optional.of(toBean(rs, beanSupplier, writers));
    } catch (Throwable e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  public List<T> beanifyAtMost(ResultSet rs, int limit) {
    Check.notNull(rs, "rs");
    Check.that(limit, "limit").is(gt(), 0);
    List<T> all = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        all.add(toBean(rs, beanSupplier, writers));
      } while (++i < limit && rs.next());
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }

  public List<T> beanifyAll(ResultSet rs) {
    return beanifyAll(rs, 16);
  }

  public List<T> beanifyAll(ResultSet rs, int sizeEstimate) {
    Check.notNull(rs, "rs");
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    List<T> all = new ArrayList<>(sizeEstimate);
    try {
      while (rs.next()) {
        all.add(toBean(rs, beanSupplier, writers));
      }
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
    return all;
  }
}
