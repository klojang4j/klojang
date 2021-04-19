package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class EmptyBeanifier<T> extends ResultSetBeanifier<T> {

  @SuppressWarnings("rawtypes")
  static final EmptyBeanifier INSTANCE = new EmptyBeanifier();

  private EmptyBeanifier() {
    super(null, null);
  }

  @Override
  public Optional<T> beanify(ResultSet rs) {
    return Optional.empty();
  }

  @Override
  public List<T> beanifyAtMost(ResultSet rs, int limit) {
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
