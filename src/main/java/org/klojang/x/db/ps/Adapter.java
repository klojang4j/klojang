package org.klojang.x.db.ps;

import java.sql.PreparedStatement;

/**
 * Converts the value from one type into another type. Adapters are used to convert arbitrary value
 * such that they can be passed to one of the {@code setXXX} methods of {@link PreparedStatement}.
 *
 * @author Ayco Holleman
 * @param <FIELD_TYPE> The type of the value to be converted, often (but not necessarily) the type
 *     of a JavaBean property
 * @param <PARAM_TYPE> The target type
 */
@FunctionalInterface
interface Adapter<FIELD_TYPE, PARAM_TYPE> {

  PARAM_TYPE adapt(FIELD_TYPE value, Class<PARAM_TYPE> targetType);
}
