package nl.naturalis.yokete.db;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class EmptyBeanifier<T> implements ResultSetBeanifier<T> {

  @SuppressWarnings("rawtypes")
  static final EmptyBeanifier INSTANCE = new EmptyBeanifier();

  private EmptyBeanifier() {}

  @Override
  public Optional<T> beanify(ResultSet rs) {
    return Optional.empty();
  }

  @Override
  public List<T> beanifyAtMost(ResultSet rs, int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<T> beanifyAtMost(ResultSet rs, int from, int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<T> beanifyAll(ResultSet rs) {
    return Collections.emptyList();
  }

  @Override
  public List<T> beanifyAll(ResultSet rs, int sizeEstimate) {
    return Collections.emptyList();
  }
}
