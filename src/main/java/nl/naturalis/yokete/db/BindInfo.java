package nl.naturalis.yokete.db;

public interface BindInfo {

  @SuppressWarnings("unused")
  default Integer getSqlType(String property, Class<?> propertyType) {
    return null;
  }

  @SuppressWarnings("unused")
  default boolean saveEnumUsingToString(String enumProperty) {
    return false;
  }
}