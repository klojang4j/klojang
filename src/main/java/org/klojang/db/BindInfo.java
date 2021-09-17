package org.klojang.db;

public interface BindInfo {

  @SuppressWarnings("unused")
  default Integer getSqlType(String propertyName, Class<?> propertyType) {
    return null;
  }

  @SuppressWarnings("unused")
  default boolean bindEnumUsingToString(String enumProperty) {
    return false;
  }
}
