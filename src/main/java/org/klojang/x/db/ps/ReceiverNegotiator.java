package org.klojang.x.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeTreeMap;
import static org.klojang.db.SQLTypeNames.getTypeName;
import static nl.naturalis.common.ClassMethods.className;
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
    Check.that(fieldType).is(keyIn(), all, "Type not supported: %s", className(fieldType));
    if (sqlType == null) {
      return (Receiver<T, U>) all.get(fieldType).getDefaultReceiver();
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Receiver<T, U> receiver = (Receiver<T, U>) all.get(fieldType).get(sqlType);
    if (receiver == null) {
      return Check.fail("Cannot convert %s to %s", sqlTypeName, className(fieldType));
    }
    return receiver;
  }

  private static Map<Class<?>, ReceiverLookup<?>> createReceivers() {
    return TypeTreeMap.build(ReceiverLookup.class)
        .autobox()
        .add(String.class, new StringReceivers())
        .add(int.class, new IntReceivers())
        .add(long.class, new LongReceivers())
        .add(double.class, new DoubleReceivers())
        .add(float.class, new FloatReceivers())
        .add(byte.class, new ByteReceivers())
        .add(boolean.class, new BooleanReceivers())
        .add(LocalDate.class, new LocalDateReceivers())
        .add(LocalDateTime.class, new LocalDateTimeReceivers())
        .add(Enum.class, new EnumReceivers())
        .freeze();
  }
}
