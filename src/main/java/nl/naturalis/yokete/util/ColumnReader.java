package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;

class ColumnReader<COLUMN_TYPE> {

  private final MethodHandle method;

  // If this is ColumnReader invokes ResultSet.getObject(int, Class), then
  // classArgument will be the Class object passed in as the second argument
  // to getObject. In anyother case classArgument will be null.
  private final Class<?> classArg;

  ColumnReader(MethodHandle method) {
    this(method, null);
  }

  ColumnReader(MethodHandle method, Class<?> classArg) {
    this.method = method;
    this.classArg = classArg;
  }

  COLUMN_TYPE readColumn(ResultSet rs, int columnIndex) throws Throwable {
    COLUMN_TYPE val;
    if (classArg == null) {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex);
    } else {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex, classArg);
    }
    return rs.wasNull() ? null : val;
  }
}
