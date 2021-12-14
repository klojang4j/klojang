package org.klojang.x.db.rs;

import java.sql.ResultSet;

/**
 * Connects a single column in the result set to a single property of the target bean, or to a
 * single key within the target map.
 *
 * @author Ayco Holleman
 * @param <T> The type of the object on which the value is set.
 */
interface Channel<T> {

  /** Copies the value of the column to the corresponding property of the target bean. */
  void send(ResultSet rs, T target) throws Throwable;

  int getSqlType();
}
