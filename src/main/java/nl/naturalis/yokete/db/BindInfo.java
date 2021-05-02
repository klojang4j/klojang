package nl.naturalis.yokete.db;

public interface BindInfo {

  @SuppressWarnings("unused")
  default Integer getSQLType(String propertyName, Class<?> propertyType) {
    return null;
  }
}
