package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nl.naturalis.common.StringMethods;

interface Writer {

  public static void checkCompatibility(ResultSet rs, Writer[] writers) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    List<String> errors = new ArrayList<>();
    if (rsmd.getColumnCount() != writers.length) {
      String fmt = "Expected column count: %d. Actual column count: %d";
      String msg = String.format(fmt, writers.length, rsmd.getColumnCount());
      errors.add(msg);
    }
    int min = Math.min(writers.length, rsmd.getColumnCount());
    for (int i = 0; i < min; ++i) {
      if (writers[i].getSqlType() != rsmd.getColumnType(i + 1)) {
        String expected = SQLTypeNames.getTypeName(writers[i].getSqlType());
        String actual = SQLTypeNames.getTypeName(rsmd.getColumnType(i + 1));
        String fmt = "Colum %3d: expected type: %s; actual type: %s";
        String msg = String.format(fmt, i + 1, expected, actual);
        errors.add(msg);
      }
    }
    if (!errors.isEmpty()) {
      String msg = StringMethods.implode(errors, ". ");
      throw new ResultSetMismatchException(msg);
    }
  }

  int getSqlType();
}
