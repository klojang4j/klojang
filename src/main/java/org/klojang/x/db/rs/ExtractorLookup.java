package org.klojang.x.db.rs;

import java.util.HashMap;
import java.util.stream.IntStream;

class ExtractorLookup<T> extends HashMap<Integer, RsExtractor<?, ?>> {

  void add(int sqlType, RsExtractor<?, T> extractor) {
    put(sqlType, extractor);
  }

  void addMore(RsExtractor<?, T> extractor, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, extractor));
  }

  RsExtractor<?, T> getDefaultExtractor() {
    return null;
  }
}
