package org.klojang.x.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import nl.naturalis.common.collection.SimpleTypeMap;
import nl.naturalis.common.collection.TypeMap;

@SuppressWarnings("rawtypes")
class DefaultReceivers {

  static final DefaultReceivers INSTANCE = new DefaultReceivers();

  private Map<Class<?>, Receiver> defaults;

  private DefaultReceivers() {
    defaults = SimpleTypeMap.build(Receiver.class)
        .autobox(true)
        .add(String.class, StringReceivers.DEFAULT)
        .add(Integer.class, IntReceivers.DEFAULT)
        .add(Boolean.class, BooleanReceivers.DEFAULT)
        .add(Double.class, DoubleReceivers.DEFAULT)
        .add(Long.class, LongReceivers.DEFAULT)
        .add(Float.class, FloatReceivers.DEFAULT)
        .add(Short.class, ShortReceivers.DEFAULT)
        .add(Byte.class, ByteReceivers.DEFAULT)
        .add(Enum.class, EnumReceivers.DEFAULT)
        .add(LocalDate.class, LocalDateReceivers.DEFAULT)
        .add(LocalDateTime.class, LocalDateTimeReceivers.DEFAULT)
        .freeze();
  }

  Receiver getDefaultReceiver(Class<?> forType) {
    return defaults.get(forType);
  }

}
