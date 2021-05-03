package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import nl.naturalis.common.ExceptionMethods;

class ResultSetIdentifier {

  private String[] colNames;
  private int[] colTypes;

  ResultSetIdentifier(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      colNames = new String[sz];
      colTypes = new int[sz];
      for (int i = 0; i < rsmd.getColumnCount(); ++i) {
        colNames[i] = rsmd.getColumnLabel(i + 1);
        colTypes[i] = rsmd.getColumnType(i + 1);
      }
    } catch (SQLException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(colNames);
    result = prime * result + Arrays.hashCode(colTypes);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    ResultSetIdentifier other = (ResultSetIdentifier) obj;
    if (colNames.length != other.colNames.length) {
      return false;
    }
    for (int i = 0; i < colNames.length; ++i) {
      if (colTypes[i] != other.colTypes[i]) return false;
      if (!colNames[i].equals(other.colNames[i])) return false;
    }
    return true;
  }
}
