package org.klojang.x.db.ps;

import java.sql.PreparedStatement;

/**
 * Converts the value of a JavaBean property such that it can be passed on the one of the {@code
 * setXXX} methods in {@link PreparedStatement}.
 *
 * @author Ayco Holleman
 * @param <PARAM_TYPE>
 * @param <FIELD_TYPE>
 */
@FunctionalInterface
interface Adapter<FIELD_TYPE, PARAM_TYPE> {

  PARAM_TYPE adapt(FIELD_TYPE value, Class<PARAM_TYPE> targetType);
}
