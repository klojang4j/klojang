package nl.naturalis.yokete.db.write;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import static nl.naturalis.common.ClassMethods.prettyClassName;
import static nl.naturalis.yokete.db.SQLTypeNames.getTypeName;

public class PersisterNegotiator {

  private static PersisterNegotiator INSTANCE;

  public static PersisterNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PersisterNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of persistersPerType that points to the persister to use if
   * the nested map does not contain a specific persister for the requested SQL type. Make sure
   * DEFAULT_ENTRY does not correspond to any of the (int) constants in the java.sql.Types class.
   */
  static final Integer DEFAULT_ENTRY = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, ValuePersister<?, ?>>> persistersPerType;

  private PersisterNegotiator() {
    persistersPerType = createPersisters();
  }

  public <T, U> ValuePersister<T, U> getDefaultPersister(Class<T> fieldType) {
    return getPersister(fieldType, DEFAULT_ENTRY);
  }

  @SuppressWarnings("unchecked")
  public <T, U> ValuePersister<T, U> getPersister(Class<T> fieldType, int sqlType) {
    if (!persistersPerType.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = sqlType == DEFAULT_ENTRY ? null : getTypeName(sqlType);
    Map<Integer, ValuePersister<?, ?>> persisters = persistersPerType.get(fieldType);
    ValuePersister<T, U> persister = (ValuePersister<T, U>) persisters.get(sqlType);
    if (persister == null) {
      if (sqlType == DEFAULT_ENTRY) {
        return Check.fail("Type not supported: %s", prettyClassName(fieldType));
      }
      return Check.fail("Cannot convert %s to %s", sqlTypeName, prettyClassName(fieldType));
    }
    return persister;
  }

  private static Map<Class<?>, Map<Integer, ValuePersister<?, ?>>> createPersisters() {
    Map<Class<?>, Map<Integer, ValuePersister<?, ?>>> tmp = new HashMap<>();
    Map<Integer, ValuePersister<?, ?>> persisters;
    tmp.put(String.class, my(new StringPersisters()));
    persisters = my(new IntPersisters());
    tmp.put(Integer.class, persisters);
    tmp.put(int.class, persisters);
    persisters = my(new BytePersisters());
    tmp.put(Byte.class, persisters);
    tmp.put(byte.class, persisters);
    persisters = my(new BooleanPersisters());
    tmp.put(Boolean.class, persisters);
    tmp.put(boolean.class, persisters);
    tmp.put(LocalDate.class, my(new LocalDatePersisters()));
    tmp.put(LocalDateTime.class, my(new LocalDateTimePersisters()));
    tmp.put(Enum.class, my(new EnumPersisters()));
    return UnmodifiableTypeMap.copyOf(tmp);
  }

  private static Map<Integer, ValuePersister<?, ?>> my(Map<Integer, ValuePersister<?, ?>> src) {
    return Map.copyOf(src);
  }
}
