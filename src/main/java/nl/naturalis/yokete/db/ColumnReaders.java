package nl.naturalis.yokete.db;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.check.Check;
import static java.sql.Types.*;
import static nl.naturalis.common.check.CommonChecks.keyIn;

class ColumnReaders {

  private static ColumnReaders INSTANCE;

  static ColumnReaders getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnReaders();
    }
    return INSTANCE;
  }

  private final Map<Integer, ColumnReader<?>> readers;

  private ColumnReaders() {
    readers = createReaderCache();
  }

  @SuppressWarnings("unchecked")
  <T> ColumnReader<T> getReader(int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), readers, "Unsupported SQL type: %s", typeName);
    return (ColumnReader<T>) readers.get(sqlType);
  }

  private static Map<Integer, ColumnReader<?>> createReaderCache() {
    Map<Integer, ColumnReader<?>> tmp = new HashMap<>();
    tmp.put(VARCHAR, ColumnReader.GET_STRING);
    tmp.put(LONGVARCHAR, ColumnReader.GET_STRING);
    tmp.put(NVARCHAR, ColumnReader.GET_STRING);
    tmp.put(LONGNVARCHAR, ColumnReader.GET_STRING);
    tmp.put(CHAR, ColumnReader.GET_STRING);
    tmp.put(CLOB, ColumnReader.GET_STRING);

    tmp.put(INTEGER, ColumnReader.GET_INT);
    tmp.put(SMALLINT, ColumnReader.GET_SHORT);
    tmp.put(TINYINT, ColumnReader.GET_BYTE);
    tmp.put(BIT, ColumnReader.GET_BYTE);
    tmp.put(DOUBLE, ColumnReader.GET_DOUBLE);
    tmp.put(REAL, ColumnReader.GET_DOUBLE);
    tmp.put(FLOAT, ColumnReader.GET_FLOAT);
    tmp.put(BIGINT, ColumnReader.GET_LONG);

    tmp.put(BOOLEAN, ColumnReader.GET_BOOLEAN);

    tmp.put(DATE, ColumnReader.GET_DATE);
    tmp.put(TIME, ColumnReader.GET_TIME);

    tmp.put(TIMESTAMP, ColumnReader.newObjectReader(LocalDateTime.class));
    tmp.put(TIMESTAMP_WITH_TIMEZONE, ColumnReader.newObjectReader(OffsetDateTime.class));

    tmp.put(NUMERIC, ColumnReader.GET_BIG_DECIMAL);
    tmp.put(DECIMAL, ColumnReader.GET_BIG_DECIMAL);

    tmp.put(ARRAY, ColumnReader.newObjectReader(Object[].class));
    return Map.copyOf(tmp);
  }
}
