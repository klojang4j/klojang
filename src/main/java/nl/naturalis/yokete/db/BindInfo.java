package nl.naturalis.yokete.db;

public interface BindInfo {

  @SuppressWarnings("unused")
  default String getLocalDateFormat(String property) {
    return "uuuu-MM-dd";
  }

  @SuppressWarnings("unused")
  default String getLocalDateTimeFormat(String property) {
    return "uuuu-MM-dd HH:mm:ss";
  }

  @SuppressWarnings("unused")
  default Integer getTargetSqlType(String property, Class<?> propertyType) {
    return null;
  }
}
