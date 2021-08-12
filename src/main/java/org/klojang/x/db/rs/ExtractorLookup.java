package org.klojang.x.db.rs;

import java.util.HashMap;
import java.util.stream.IntStream;

class ExtractorLookup<T> extends HashMap<Integer, RsExtractor<?, ?>> {

  ExtractorLookup() {
    super();
  }

  private ExtractorLookup(int initialCapacity) {
    super(initialCapacity);
  }

  void add(int sqlType, RsExtractor<?, T> extractor) {
    put(sqlType, extractor);
  }

  void addMultiple(RsExtractor<?, T> extractor, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, extractor));
  }

  RsExtractor<?, T> getDefaultExtractor() {
    return null;
  }

  ExtractorLookup<T> copy() {
    ExtractorLookup<T> copy = new ExtractorLookup<>((size() * 4) / 3 + 1);
    copy.putAll(this);
    return copy;
  }
}
