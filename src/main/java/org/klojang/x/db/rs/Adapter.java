package org.klojang.x.db.rs;

/*
 * Converts a value retrieved through one of the {@code ResultSet.getXXX} methods such that it can
 * be assigned to a JavaBean property.
 */
@FunctionalInterface
interface Adapter<COLUMN_TYPE, FIELD_TYPE> {

  FIELD_TYPE adapt(COLUMN_TYPE value, Class<FIELD_TYPE> targetType);
}
