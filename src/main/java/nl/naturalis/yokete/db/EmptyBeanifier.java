package nl.naturalis.yokete.db;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class EmptyBeanifier<T> implements ResultSetBeanifier<T> {

  @SuppressWarnings("rawtypes")
  static final EmptyBeanifier INSTANCE = new EmptyBeanifier();

  private EmptyBeanifier() {}

  @Override
  public Optional<T> beanify() {
    return Optional.empty();
  }

  @Override
  public List<T> beanifyAtMost(int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<T> beanifyAtMost(int from, int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<T> beanifyAll() {
    return Collections.emptyList();
  }

  @Override
  public List<T> beanifyAll(int sizeEstimate) {
    return Collections.emptyList();
  }

  @Override
  public void close() {}
}
