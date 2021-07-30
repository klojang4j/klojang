package org.klojang.x.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import static org.klojang.db.SQLTypeNames.getTypeName;
import static nl.naturalis.common.ClassMethods.getPrettyClassName;
import static nl.naturalis.common.check.CommonChecks.keyIn;

class ReceiverNegotiator {

  private static ReceiverNegotiator INSTANCE;

  static ReceiverNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ReceiverNegotiator();
    }
    return INSTANCE;
  }

  private final Map<Class<?>, ReceiverLookup<?>> all;

  private ReceiverNegotiator() {
    all = createReceivers();
  }

  <T, U> Receiver<T, U> getDefaultReceiver(Class<T> fieldType) {
    return findReceiver(fieldType, null);
  }

  @SuppressWarnings("unchecked")
  <T, U> Receiver<T, U> findReceiver(Class<T> fieldType, Integer sqlType) {
    Check.that(fieldType).is(keyIn(), all, "Type not supported: %s", getPrettyClassName(fieldType));
    if (sqlType == null) {
      return (Receiver<T, U>) all.get(fieldType).getDefaultReceiver();
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Receiver<T, U> receiver = (Receiver<T, U>) all.get(fieldType).get(sqlType);
    if (receiver == null) {
      return Check.fail("Cannot convert %s to %s", sqlTypeName, getPrettyClassName(fieldType));
    }
    return receiver;
  }

  private static Map<Class<?>, ReceiverLookup<?>> createReceivers() {
    TypeMap<ReceiverLookup<?>> map = new TypeMap<>();
    ReceiverLookup<?> receivers;
    map.put(String.class, new StringReceivers());
    receivers = new IntReceivers();
    map.put(Integer.class, receivers);
    map.put(int.class, receivers);
    receivers = new LongReceivers();
    map.put(Long.class, receivers);
    map.put(long.class, receivers);
    receivers = new DoubleReceivers();
    map.put(Double.class, receivers);
    map.put(double.class, receivers);
    receivers = new ByteReceivers();
    map.put(Byte.class, receivers);
    map.put(byte.class, receivers);
    receivers = new BooleanReceivers();
    map.put(Boolean.class, receivers);
    map.put(boolean.class, receivers);
    map.put(LocalDate.class, new LocalDateReceivers());
    map.put(LocalDateTime.class, new LocalDateTimeReceivers());
    map.put(Enum.class, new EnumReceivers());
    return map;
  }
}
