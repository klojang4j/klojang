package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;

public class RsStrongIdentifier {

  private final String[] colNames;
  private final int[] colTypes;

  public RsStrongIdentifier(ResultSet rs) {
    Check.notNull(rs);
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

  private int hash = 0;

  @Override
  public int hashCode() {
    if (hash == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(colNames);
      result = prime * result + Arrays.hashCode(colTypes);
      hash = result;
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    RsStrongIdentifier other = (RsStrongIdentifier) obj;
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
