package nl.naturalis.yokete.db.rs;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.ModulePrivate;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.SQLTypeNames;
import static java.sql.Types.*;
import static nl.naturalis.common.check.CommonChecks.keyIn;
import static nl.naturalis.yokete.db.rs.RSGetter.*;

@ModulePrivate
public class RSGetters {

  private static RSGetters INSTANCE;

  public static RSGetters getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new RSGetters();
    }
    return INSTANCE;
  }

  private final Map<Integer, RSGetter<?>> cache;

  private RSGetters() {
    cache = createCache();
  }

  @SuppressWarnings("unchecked")
  public <T> RSGetter<T> getReader(int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), cache, "Unsupported SQL type: %s", typeName);
    return (RSGetter<T>) cache.get(sqlType);
  }

  private static Map<Integer, RSGetter<?>> createCache() {
    Map<Integer, RSGetter<?>> tmp = new HashMap<>();
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
