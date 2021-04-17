package nl.naturalis.yokete.util;

import java.util.function.Function;

class Synapse<T> {

  private final ResultSetGetter rsGetter;
  private final Function<?, T> adapter;

  Synapse(ResultSetGetter getter) {
    this(getter, Function.identity());
  }

  Synapse(ResultSetGetter getter, Function<?, T> adapter) {
    this.rsGetter = getter;
    this.adapter = adapter;
  }
}
