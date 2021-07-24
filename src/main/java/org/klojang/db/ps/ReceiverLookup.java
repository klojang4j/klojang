package org.klojang.db.ps;

import java.util.HashMap;
import java.util.stream.IntStream;

abstract class ReceiverLookup<T> extends HashMap<Integer, Receiver<?, ?>> {

  ReceiverLookup() {}

  void putMultiple(Receiver<T, ?> receiver, int... sqlTypes) {
    IntStream.of(sqlTypes).forEach(i -> put(i, receiver));
  }

  abstract Receiver<T, ?> getDefaultReceiver();
}
