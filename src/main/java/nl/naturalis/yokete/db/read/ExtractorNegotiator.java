package nl.naturalis.yokete.db.read;

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
class ExtractorNegotiator {

  private static ExtractorNegotiator INSTANCE;

  static ExtractorNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ExtractorNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of the extractorsByType that points to the extractor to use if
   * the nested map does not contain a specific extractor for the requested SQL type. Make sure
   * DEFAULT_ENTRY does not correspond to any of the (int) constants in the java.sql.Types class.
   */
  static final Integer DEFAULT_ENTRY = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, ValueExtractor<?, ?>>> extractorsByType;

  private ExtractorNegotiator() {
    extractorsByType = createExtractors();
  }

  @SuppressWarnings("unchecked")
  <T, U> ValueExtractor<T, U> getProducer(Class<U> fieldType, int sqlType) {
    if (!extractorsByType.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = sqlType == DEFAULT_ENTRY ? null : getTypeName(sqlType);
    Map<Integer, ValueExtractor<?, ?>> extractors = extractorsByType.get(fieldType);
    if (!extractors.containsKey(sqlType)) {
      if (extractors.containsKey(DEFAULT_ENTRY)) {
        return (ValueExtractor<T, U>) extractors.get(DEFAULT_ENTRY);
      }
      return Check.fail("Cannot convert %s to %s", sqlTypeName, prettyClassName(fieldType));
    }
    return (ValueExtractor<T, U>) extractors.get(sqlType);
  }

  private static Map<Class<?>, Map<Integer, ValueExtractor<?, ?>>> createExtractors() {
    Map<Class<?>, Map<Integer, ValueExtractor<?, ?>>> tmp = new HashMap<>();
    Map<Integer, ValueExtractor<?, ?>> producers;
    tmp.put(String.class, my(new StringExtractors()));
    producers = my(new IntExtractors());
    tmp.put(Integer.class, producers);
    tmp.put(int.class, producers);
    producers = my(new LongExtractors());
    tmp.put(Long.class, producers);
    tmp.put(long.class, producers);
    producers = my(new ByteExtractors());
    tmp.put(Byte.class, producers);
    tmp.put(byte.class, producers);
    producers = my(new BooleanExtractors());
    tmp.put(Boolean.class, producers);
    tmp.put(boolean.class, producers);
    tmp.put(LocalDate.class, my(new LocalDateExtractors()));
    tmp.put(LocalDateTime.class, my(new LocalDateTimeExtractors()));
    tmp.put(Enum.class, new EnumExtractors());
    return UnmodifiableTypeMap.copyOf(tmp);
  }

  private static Map<Integer, ValueExtractor<?, ?>> my(Map<Integer, ValueExtractor<?, ?>> src) {
    return Map.copyOf(src);
  }
}
