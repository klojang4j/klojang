package nl.naturalis.yokete.db.rs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import static nl.naturalis.common.ClassMethods.prettyClassName;
import static nl.naturalis.yokete.db.SQLTypeNames.getTypeName;

/**
 * Finds the most suitable of the {@code ResultSet.getXXX} methods for a given Java type. If no
 * sure-fire match can be found, then an {@link Adapter} function can be specified that converts the
 * result of the {@code getXXX} method to the required Java type. Therefore what actually gets
 * negotiated is not so much the {@code getXXX} method per se, but an {@link RsExtractor}, which
 * combines a {@code getXXX} method with an (optional) converter function.
 */
@ModulePrivate
public class ExtractorNegotiator {

  private static ExtractorNegotiator INSTANCE;

  public static ExtractorNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ExtractorNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of the emittersByType that points to the Emitter to use if the
   * nested map does not contain a specific Emitter for the requested SQL type. Make sure DEFAULT
   * does not correspond to any of the int constants in the java.sql.Types class.
   */
  static final Integer DEFAULT = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, RsExtractor<?, ?>>> emittersByType;

  private ExtractorNegotiator() {
    emittersByType = createEmitters();
  }

  @SuppressWarnings("unchecked")
  public <T, U> RsExtractor<T, U> findExtractor(Class<U> fieldType, int sqlType) {
    if (!emittersByType.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = sqlType == DEFAULT ? null : getTypeName(sqlType);
    Map<Integer, RsExtractor<?, ?>> extractors = emittersByType.get(fieldType);
    if (!extractors.containsKey(sqlType)) {
      if (extractors.containsKey(DEFAULT)) {
        return (RsExtractor<T, U>) extractors.get(DEFAULT);
      }
      return Check.fail("Cannot convert %s to %s", sqlTypeName, prettyClassName(fieldType));
    }
    return (RsExtractor<T, U>) extractors.get(sqlType);
  }

  private static Map<Class<?>, Map<Integer, RsExtractor<?, ?>>> createEmitters() {
    TypeMap<Map<Integer, RsExtractor<?, ?>>> map = new TypeMap<>();
    Map<Integer, RsExtractor<?, ?>> extractors;
    map.put(String.class, my(new StringExtractors()));
    extractors = my(new IntExtractors());
    map.put(Integer.class, extractors);
    map.put(int.class, extractors);
    extractors = my(new LongExtractors());
    map.put(Long.class, extractors);
    map.put(long.class, extractors);
    extractors = my(new ByteExtractors());
    map.put(Byte.class, extractors);
    map.put(byte.class, extractors);
    extractors = my(new BooleanExtractors());
    map.put(Boolean.class, extractors);
    map.put(boolean.class, extractors);
    map.put(LocalDate.class, my(new LocalDateExtractors()));
    map.put(LocalDateTime.class, my(new LocalDateTimeExtractors()));
    map.put(Enum.class, new EnumExtractors());
    return map;
  }

  private static Map<Integer, RsExtractor<?, ?>> my(Map<Integer, RsExtractor<?, ?>> src) {
    return Map.copyOf(src);
  }
}
