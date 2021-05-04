package nl.naturalis.yokete.db.ps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.FlatTypeMap;
import static nl.naturalis.common.ClassMethods.prettyClassName;
import static nl.naturalis.yokete.db.SQLTypeNames.getTypeName;

public class ReceiverNegotiator {

  private static ReceiverNegotiator INSTANCE;

  public static ReceiverNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ReceiverNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of receiversByType that points to the Receiver to use if
   * the nested map does not contain a specific Receiver for the requested SQL type. Make sure
   * DEFAULT_ENTRY does not correspond to any of the int constants in the java.sql.Types class.
   */
  static final Integer DEFAULT = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, Receiver<?, ?>>> receiversByType;

  private ReceiverNegotiator() {
    receiversByType = createReceivers();
    System.out.println(receiversByType);
  }

  public <T, U> Receiver<T, U> getDefaultReceiver(Class<T> fieldType) {
    return getReceiver(fieldType, DEFAULT);
  }

  @SuppressWarnings("unchecked")
  public <T, U> Receiver<T, U> getReceiver(Class<T> fieldType, int sqlType) {
    if (!receiversByType.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = sqlType == DEFAULT ? null : getTypeName(sqlType);
    Map<Integer, Receiver<?, ?>> receivers = receiversByType.get(fieldType);
    Receiver<T, U> receiver = (Receiver<T, U>) receivers.get(sqlType);
    if (receiver == null) {
      if (sqlType == DEFAULT) {
        return Check.fail("Type not supported: %s", prettyClassName(fieldType));
      }
      return Check.fail("Cannot convert %s to %s", sqlTypeName, prettyClassName(fieldType));
    }
    return receiver;
  }

  private static Map<Class<?>, Map<Integer, Receiver<?, ?>>> createReceivers() {
    FlatTypeMap<Map<Integer, Receiver<?, ?>>> map = new FlatTypeMap<>();
    Map<Integer, Receiver<?, ?>> receivers;
    map.put(String.class, my(new StringReceivers()));
    receivers = my(new IntReceivers());
    map.put(Integer.class, receivers);
    map.put(int.class, receivers);
    receivers = my(new LongReceivers());
    map.put(Long.class, receivers);
    map.put(long.class, receivers);
    receivers = my(new DoubleReceivers());
    map.put(Double.class, receivers);
    map.put(double.class, receivers);
    receivers = my(new ByteReceivers());
    map.put(Byte.class, receivers);
    map.put(byte.class, receivers);
    receivers = my(new BooleanReceivers());
    map.put(Boolean.class, receivers);
    map.put(boolean.class, receivers);
    map.put(LocalDate.class, my(new LocalDateReceivers()));
    map.put(LocalDateTime.class, my(new LocalDateTimeReceivers()));
    map.put(Enum.class, my(new EnumReceivers()));
    return map;
  }

  private static Map<Integer, Receiver<?, ?>> my(Map<Integer, Receiver<?, ?>> src) {
    return Map.copyOf(src);
  }
}
