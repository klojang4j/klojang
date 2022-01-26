package org.klojang.x.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.collection.TypeMap;

@SuppressWarnings("rawtypes")
class DefaultReceivers {

  static final DefaultReceivers INSTANCE = new DefaultReceivers();

  private Map<Class<?>, Receiver> defaults;

  private DefaultReceivers() {
    defaults =
        TypeMap.build(Receiver.class)
            .autobox(true)
            .add(String.class, StringReceivers.DEFAULT)
            .add(int.class, IntReceivers.DEFAULT)
            .add(boolean.class, BooleanReceivers.DEFAULT)
            .add(double.class, DoubleReceivers.DEFAULT)
            .add(long.class, LongReceivers.DEFAULT)
            .add(float.class, FloatReceivers.DEFAULT)
            .add(short.class, ShortReceivers.DEFAULT)
            .add(byte.class, ByteReceivers.DEFAULT)
            .add(Enum.class, EnumReceivers.DEFAULT)
            .add(LocalDate.class, LocalDateReceivers.DEFAULT)
            .add(LocalDateTime.class, LocalDateTimeReceivers.DEFAULT)
            .freeze();
  }

  Receiver getDefaultReceiver(Class<?> forType) {
    return defaults.get(forType);
  }
}
