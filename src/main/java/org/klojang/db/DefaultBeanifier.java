package org.klojang.db;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Supplier;
import org.klojang.x.db.rs.BeanChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.check.Check;

import static nl.naturalis.common.check.CommonExceptions.STATE;
import static org.klojang.x.db.rs.BeanChannel.toBean;
import static nl.naturalis.common.check.CommonChecks.gt;
import static nl.naturalis.common.check.CommonExceptions.illegalState;
import static nl.naturalis.common.check.CommonChecks.yes;

class DefaultBeanifier<T> implements ResultSetBeanifier<T> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DefaultBeanifier.class);

  private class BeanIterator implements Iterator<T> {

    DefaultBeanifier<T> beanifier;

    BeanIterator(DefaultBeanifier<T> beanifier) {
      this.beanifier = beanifier;
    }

    @Override
    public boolean hasNext() {
      return beanifier.hasMore;
    }

    @Override
    public T next() {
      Check.on(STATE, beanifier.hasMore).is(yes(), "No more rows in result set");
      return beanifier.beanify().get();
    }
  }

  private final ResultSet rs;
  private final BeanChannel<?, ?>[] channels;
  private final Supplier<T> beanSupplier;

  private boolean hasMore = true;

  DefaultBeanifier(ResultSet rs, BeanChannel<?, ?>[] channels, Supplier<T> supplier) {
    this.rs = rs;
    this.channels = channels;
    this.beanSupplier = supplier;
  }

  @Override
  public Optional<T> beanify() {
    if (!hasMore) {
      return Optional.empty();
    }
    try {
      Optional<T> bean = Optional.of(toBean(rs, beanSupplier, channels));
      hasMore = rs.next();
      return bean;
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, null);
    }
  }

  @Override
  public List<T> beanify(int limit) {
    Check.that(limit, "limit").is(gt(), 0);
    if (!hasMore) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(limit);
    int i = 0;
    try {
      do {
        beans.add(toBean(rs, beanSupplier, channels));
      } while (++i < limit && (hasMore = rs.next()));
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, null);
    }
    return beans;
  }

  @Override
  public List<T> beanifyAll() {
    return beanifyAll(10);
  }

  @Override
  public List<T> beanifyAll(int sizeEstimate) {
    Check.that(sizeEstimate, "sizeEstimate").is(gt(), 0);
    if (!hasMore) {
      return Collections.emptyList();
    }
    List<T> beans = new ArrayList<>(sizeEstimate);
    try {
      do {
        beans.add(toBean(rs, beanSupplier, channels));
      } while (rs.next());
    } catch (Throwable t) {
      throw KJSQLException.wrap(t, null);
    }
    hasMore = false;
    return beans;
  }

  @Override
  public boolean isEmpty() {
    return !hasMore;
  }

  @Override
  public Iterator<T> iterator() {
    return new BeanIterator(this);
  }
}
