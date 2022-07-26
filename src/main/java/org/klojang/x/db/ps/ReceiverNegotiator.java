package org.klojang.x.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import nl.naturalis.check.Check;
import nl.naturalis.common.collection.TypeGraph;

import static org.klojang.db.SQLTypeNames.getTypeName;
import static nl.naturalis.common.ClassMethods.className;
import static nl.naturalis.check.CommonChecks.notNull;

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
    Map tmp = createReceivers(); // to hell with generics
    all = (Map<Class<?>, Map<Integer, Receiver>>) tmp;
  }

  <T, U> Receiver<T, U> getDefaultReceiver(Class<T> fieldType) {
    Receiver receiver = DefaultReceivers.INSTANCE.getDefaultReceiver(fieldType);
    return Check.that(receiver).is(notNull(), "Type not supported: {type}").ok();
  }

  <T, U> Receiver<T, U> findReceiver(Class<T> fieldType, int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = getTypeName(sqlType);
    Map<Integer, Receiver> receivers = all.get(fieldType);
    Check.that(receivers).is(notNull(), "Type not supported: {type}");
    Receiver<T, U> receiver = receivers.get(sqlType);
    Check.that(receiver).is(notNull(), "Cannot convert {0} to {type}", sqlTypeName);
    return receiver;
  }

  private static Map createReceivers() {
    return TypeGraph.build(Map.class)
        .autobox(true)
        .add(String.class, my(new StringReceivers()))
        .add(Integer.class, my(new IntReceivers()))
        .add(Long.class, my(new LongReceivers()))
        .add(Double.class, my(new DoubleReceivers()))
        .add(Float.class, my(new FloatReceivers()))
        .add(Short.class, my(new ShortReceivers()))
        .add(Byte.class, my(new ByteReceivers()))
        .add(Boolean.class, my(new BooleanReceivers()))
        .add(LocalDate.class, my(new LocalDateReceivers()))
        .add(LocalDateTime.class, my(new LocalDateTimeReceivers()))
        .add(Enum.class, my(new EnumReceivers()))
        .freeze();
  }

  private static Map<Integer, Receiver> my(ReceiverLookup src) {
    return Map.copyOf(src);
  }

}
