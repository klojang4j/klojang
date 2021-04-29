package nl.naturalis.yokete.db.write;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.UnmodifiableTypeMap;
import nl.naturalis.yokete.db.SQLTypeNames;
import static nl.naturalis.common.ClassMethods.prettyClassName;

public class ValueAbsorberNegotiator {

  private static ValueAbsorberNegotiator INSTANCE;

  public static ValueAbsorberNegotiator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ValueAbsorberNegotiator();
    }
    return INSTANCE;
  }

  /**
   * The entry within the nested maps of the dendrites that points to the Dendrite to use for a Java
   * type if the nested map does not contain a specific Dendrite for the requested SQL type. Make
   * sure DEFAULT_ENTRY does not correspond to any of the (int) constants in the java.sql.Types
   * class. If there is no reasonable default, you don't need to specify one.
   */
  static final Integer DEFAULT_ENTRY = Integer.MIN_VALUE;

  private final Map<Class<?>, Map<Integer, ValueAbsorber<?, ?>>> absorbers;

  private ValueAbsorberNegotiator() {
    absorbers = createAbsorbers();
  }

  public <FIELD_TYPE, COLUMN_TYPE> ValueAbsorber<FIELD_TYPE, COLUMN_TYPE> getDefaultDendrite(
      Class<FIELD_TYPE> fieldType) {
    return getDendrite(fieldType, DEFAULT_ENTRY);
  }

  @SuppressWarnings("unchecked")
  public <FIELD_TYPE, COLUMN_TYPE> ValueAbsorber<FIELD_TYPE, COLUMN_TYPE> getDendrite(
      Class<FIELD_TYPE> fieldType, int sqlType) {
    if (!absorbers.containsKey(fieldType)) {
      return Check.fail("Type not supported: %s", prettyClassName(fieldType));
    }
    ValueAbsorber<FIELD_TYPE, COLUMN_TYPE> dendrite =
        (ValueAbsorber<FIELD_TYPE, COLUMN_TYPE>) absorbers.get(fieldType).get(sqlType);
    if (dendrite == null) {
      if (sqlType == DEFAULT_ENTRY) {
        throw new AssertionError("Missing default absorber"); // programming error
      }
      String s0 = SQLTypeNames.getTypeName(sqlType);
      String s1 = prettyClassName(fieldType);
      return Check.fail("Cannot convert %s to %s", s0, s1);
    }
    return dendrite;
  }

  private static Map<Class<?>, Map<Integer, ValueAbsorber<?, ?>>> createAbsorbers() {
    Map<Class<?>, Map<Integer, ValueAbsorber<?, ?>>> tmp = new HashMap<>();

    Map<Integer, ValueAbsorber<?, ?>> absorbers;
    tmp.put(String.class, DendritesForString.get());

    absorbers = my(new IntAbsorbers());
    tmp.put(Integer.class, absorbers);
    tmp.put(int.class, absorbers);

    absorbers = ByteAbsorbers.get();
    tmp.put(Byte.class, absorbers);
    tmp.put(byte.class, absorbers);

    absorbers = my(new BooleanAbsorbers());
    tmp.put(Boolean.class, absorbers);
    tmp.put(boolean.class, absorbers);

    tmp.put(LocalDate.class, DendritesForLocalDate.get());
    tmp.put(LocalDateTime.class, DendritesForLocalDateTime.get());

    tmp.put(Enum.class, my(new EnumAbsorbers()));

    // TODO: more of this

    return UnmodifiableTypeMap.copyOf(tmp);
  }

  private static Map<Integer, ValueAbsorber<?, ?>> my(Map<Integer, ValueAbsorber<?, ?>> src) {
    return Map.copyOf(src);
  }
}
