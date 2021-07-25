package org.klojang.x.db.rs;

/**
 * Transports a single value from a {@code ResultSet} to a JavaBean or <code>
 * Map&lt;String,Object&gt;</code>. It embodies the very last step in the journey from {@code
 * ResultSet} to bean/map.
 */
interface ValueTransporter {

  int getSqlType();
}
