package org.klojang.x.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeTreeMap;
import static org.klojang.db.SQLTypeNames.getTypeName;
import static nl.naturalis.common.ClassMethods.className;
import static nl.naturalis.common.check.CommonChecks.notNull;

@SuppressWarnings({"rawtypes", "unchecked"})
class ReceiverNegotiator {

  private static ReceiverNegotiator INSTANCE;

  static ReceiverNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ReceiverNegotiator();
    }
    return INSTANCE;
  }

  private final Map<Class<?>, Map<Integer, Receiver>> all;

  private ReceiverNegotiator() {
    all = createReceivers();
  }

  <T, U> Receiver<T, U> getDefaultReceiver(Class<T> fieldType) {
    return Check.that(DefaultReceivers.INSTANCE.getDefaultReceiver(fieldType))
        .is(notNull(), "Type not supported: %s", className(fieldType))
        .ok();
  }

  <T, U> Receiver<T, U> findReceiver(Class<T> fieldType, int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, Receiver> receivers =
        Check.that(all.get(fieldType))
            .is(notNull(), "Type not supported: %s", className(fieldType))
            .ok();
    return Check.that(receivers.get(sqlType))
        .is(notNull(), "Cannot convert %s to %s", sqlTypeName, className(fieldType))
        .ok();
  }

  private static Map<Class<?>, Map<Integer, Receiver>> createReceivers() {
    return TypeTreeMap.build(Map.class)
        .autobox(true)
        .add(String.class, my(new StringReceivers()))
        .add(int.class, my(new IntReceivers()))
        .add(long.class, my(new LongReceivers()))
        .add(double.class, my(new DoubleReceivers()))
        .add(float.class, my(new FloatReceivers()))
        .add(short.class, my(new ShortReceivers()))
        .add(byte.class, my(new ByteReceivers()))
        .add(boolean.class, my(new BooleanReceivers()))
        .add(LocalDate.class, my(new LocalDateReceivers()))
        .add(LocalDateTime.class, my(new LocalDateTimeReceivers()))
        .add(Enum.class, my(new EnumReceivers()))
        .bump(String.class)
        .freeze();
  }

  private static Map<Integer, Receiver> my(ReceiverLookup src) {
    return Map.copyOf(src);
  }
}
