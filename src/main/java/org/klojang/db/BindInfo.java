package org.klojang.db;

import java.sql.PreparedStatement;

/**
 * {@code BindInfo} objects allow you to instruct how values should be bound into a {@link
 * PreparedStatement}.
 *
 * @author Ayco Holleman
 */
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
