package nl.naturalis.yokete.db.write;

import java.sql.PreparedStatement;

/**
 * Converts the value of a JavaBean property such that it can be passed on the one of the {@code
 * setXXX} methods in {@link PreparedStatement}.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
@FunctionalInterface
interface Adapter<FIELD_TYPE, COLUMN_TYPE> {

  COLUMN_TYPE adapt(FIELD_TYPE value, Class<COLUMN_TYPE> targetType);
}
