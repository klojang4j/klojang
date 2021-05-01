package nl.naturalis.yokete.db.rs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import static nl.naturalis.common.ClassMethods.prettyClassName;
import static nl.naturalis.yokete.db.SQLTypeNames.getTypeName;

/**
 * Finds the most suitable of the ResultSet.getXXX methods for a given Java type. If no sure-fire
 * match can be found, then an "adapter" function can be specified that converts the result of the
 * getXXX method to the required Java type. Therefore what actually gets negotiated is not so much a
 * ResultSet.getXXX method per se, but a Synapse, which is a combination of a ResultSet.getXXX
 * method and a converter function.
 */
class EmitterSelector {

  private static EmitterSelector INSTANCE;

  static EmitterSelector getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EmitterSelector();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of the emittersByType that points to the Emitter to use if the
   * nested map does not contain a specific Emitter for the requested SQL type. Make sure DEFAULT
   * does not correspond to any of the int constants in the java.sql.Types class.
   */
  static final Integer DEFAULT = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, Emitter<?, ?>>> emittersByType;

  private EmitterSelector() {
    emittersByType = createEmitters();
  }

  @SuppressWarnings("unchecked")
  <T, U> Emitter<T, U> getProducer(Class<U> fieldType, int sqlType) {
    if (!emittersByType.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = sqlType == DEFAULT ? null : getTypeName(sqlType);
    Map<Integer, Emitter<?, ?>> emitters = emittersByType.get(fieldType);
    if (!emitters.containsKey(sqlType)) {
      if (emitters.containsKey(DEFAULT)) {
        return (Emitter<T, U>) emitters.get(DEFAULT);
      }
      return Check.fail("Cannot convert %s to %s", sqlTypeName, prettyClassName(fieldType));
    }
    return (Emitter<T, U>) emitters.get(sqlType);
  }

  private static Map<Class<?>, Map<Integer, Emitter<?, ?>>> createEmitters() {
    Map<Class<?>, Map<Integer, Emitter<?, ?>>> tmp = new HashMap<>();
    Map<Integer, Emitter<?, ?>> emitters;
    tmp.put(String.class, my(new StringEmitters()));
    emitters = my(new IntEmitters());
    tmp.put(Integer.class, emitters);
    tmp.put(int.class, emitters);
    emitters = my(new LongEmitters());
    tmp.put(Long.class, emitters);
    tmp.put(long.class, emitters);
    emitters = my(new ByteEmitters());
    tmp.put(Byte.class, emitters);
    tmp.put(byte.class, emitters);
    emitters = my(new BooleanEmitters());
    tmp.put(Boolean.class, emitters);
    tmp.put(boolean.class, emitters);
    tmp.put(LocalDate.class, my(new LocalDateEmitters()));
    tmp.put(LocalDateTime.class, my(new LocalDateTimeEmitters()));
    tmp.put(Enum.class, new EnumEmitters());
    return UnmodifiableTypeMap.copyOf(tmp);
  }

  private static Map<Integer, Emitter<?, ?>> my(Map<Integer, Emitter<?, ?>> src) {
    return Map.copyOf(src);
  }
}
