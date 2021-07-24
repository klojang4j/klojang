package org.klojang.db.rs;

import java.sql.ResultSet;

/**
 * Converts a value retrieved through one of the {@code getXXX} methods in {@link ResultSet} such
 * that it can be assigned to a JavaBean property.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
@FunctionalInterface
interface Adapter<COLUMN_TYPE, FIELD_TYPE> {

  /**
   * Converts the value retrieved through one of the {@code ResultSet.getXXX} methods to the type of
   * the bean field that the value is destined for.
   *
   * @param value
   * @param targetType
   * @return
   */
  FIELD_TYPE adapt(COLUMN_TYPE value, Class<FIELD_TYPE> targetType);
}
