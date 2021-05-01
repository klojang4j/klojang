package nl.naturalis.yokete.db;

public interface BindConfig {

  @SuppressWarnings("unused")
  default Integer getSQLType(String propertyName, Class<?> propertyType) {
    return null;
  }
}
