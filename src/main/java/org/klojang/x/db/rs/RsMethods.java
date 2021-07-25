package org.klojang.x.db.rs;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.klojang.db.SQLTypeNames;
import nl.naturalis.common.check.Check;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;
import static nl.naturalis.common.check.CommonChecks.keyIn;

class RsMethods {

  private static RsMethods INSTANCE;

  static RsMethods getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new RsMethods();
    }
    return INSTANCE;
  }

  private final Map<Integer, RsMethod<?>> cache;

  private RsMethods() {
    cache = createCache();
  }

  @SuppressWarnings("unchecked")
  <T> RsMethod<T> getMethod(int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), cache, "Unsupported SQL type: %s", typeName);
    return (RsMethod<T>) cache.get(sqlType);
  }

  private static Map<Integer, RsMethod<?>> createCache() {
    Map<Integer, RsMethod<?>> tmp = new HashMap<>();
    tmp.put(VARCHAR, GET_STRING);
    tmp.put(LONGVARCHAR, GET_STRING);
    tmp.put(NVARCHAR, GET_STRING);
    tmp.put(LONGNVARCHAR, GET_STRING);
    tmp.put(CHAR, GET_STRING);
    tmp.put(CLOB, GET_STRING);

    tmp.put(INTEGER, GET_INT);
    tmp.put(SMALLINT, GET_SHORT);
    tmp.put(TINYINT, GET_BYTE);
    tmp.put(BIT, GET_BYTE);
    tmp.put(DOUBLE, GET_DOUBLE);
    tmp.put(REAL, GET_DOUBLE);
    tmp.put(FLOAT, GET_FLOAT);
    tmp.put(BIGINT, GET_LONG);

    tmp.put(BOOLEAN, GET_BOOLEAN);

    tmp.put(DATE, GET_DATE);
    tmp.put(TIME, GET_TIME);

    tmp.put(TIMESTAMP, objectGetter(LocalDateTime.class));
    tmp.put(TIMESTAMP_WITH_TIMEZONE, objectGetter(OffsetDateTime.class));

    tmp.put(NUMERIC, GET_BIG_DECIMAL);
    tmp.put(DECIMAL, GET_BIG_DECIMAL);

    tmp.put(ARRAY, objectGetter(Object[].class));
    return Map.copyOf(tmp);
  }
}
