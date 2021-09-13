package org.klojang.x.db.rs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.TypeMap;
import static org.klojang.db.SQLTypeNames.getTypeName;
import static nl.naturalis.common.ClassMethods.className;

/**
 * Finds the most suitable of the {@code ResultSet.getXXX} methods for a given Java type. If no
 * sure-fire match can be found, then an {@link Adapter} function can be specified that converts the
 * result of the {@code getXXX} method to the required Java type. Therefore what actually gets
 * negotiated is not so much the {@code getXXX} method per se, but an {@link RsExtractor}, which
 * combines a {@code getXXX} method with an (optional) converter function.
 */
@ModulePrivate
@SuppressWarnings("rawtypes")
public class ExtractorNegotiator {

  private static ExtractorNegotiator INSTANCE;

  public static ExtractorNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ExtractorNegotiator();
    }
    return INSTANCE;
  }

  /*
   * The entry within the nested maps of the extractorsByType that points to the Emitter to use if the
   * nested map does not contain a specific Emitter for the requested SQL type. Make sure DEFAULT
   * does not correspond to any of the int constants in the java.sql.Types class.
   */
  static final Integer DEFAULT = Integer.MIN_VALUE;

  private final TypeMap<Map<Integer, RsExtractor>> extractorsByType;

  private ExtractorNegotiator() {
    extractorsByType = configure();
  }

  public <T, U> RsExtractor<T, U> findExtractor(Class<U> fieldType, int sqlType) {
    if (!extractorsByType.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", className(fieldType));
    }
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String sqlTypeName = sqlType == DEFAULT ? null : getTypeName(sqlType);
    Map<Integer, RsExtractor> extractors = extractorsByType.get(fieldType);
    if (!extractors.containsKey(sqlType)) {
      if (extractors.containsKey(DEFAULT)) {
        return extractors.get(DEFAULT);
      }
      return Check.fail("Cannot convert %s to %s", sqlTypeName, className(fieldType));
    }
    return extractors.get(sqlType);
  }

  private static TypeMap<Map<Integer, RsExtractor>> configure() {
    return TypeMap.build(Map.class)
        .autobox()
        .add(String.class, my(new StringExtractors()))
        .add(int[].class, my(new IntExtractors()))
        .add(double.class, my(new DoubleExtractors()))
        .add(long.class, my(new LongExtractors()))
        .add(float.class, my(new FloatExtractors()))
        .add(short.class, my(new ShortExtractors()))
        .add(byte.class, my(new ByteExtractors()))
        .add(boolean.class, my(new BooleanExtractors()))
        .add(LocalDate.class, my(new LocalDateExtractors()))
        .add(LocalDateTime.class, my(new LocalDateTimeExtractors()))
        .add(Enum.class, my(new EnumExtractors()))
        .freeze();
  }

  private static Map<Integer, RsExtractor> my(ExtractorLookup<?> src) {
    return Map.copyOf(src);
  }
}
