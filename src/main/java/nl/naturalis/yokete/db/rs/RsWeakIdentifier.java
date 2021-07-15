package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;

public class RsWeakIdentifier {

  private final int[] colTypes;

  public RsWeakIdentifier(ResultSet rs) {
    Check.notNull(rs);
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      colTypes = new int[sz];
      for (int i = 0; i < rsmd.getColumnCount(); ++i) {
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
    RsWeakIdentifier other = (RsWeakIdentifier) obj;
    for (int i = 0; i < colTypes.length; ++i) {
      if (colTypes[i] != other.colTypes[i]) return false;
    }
    return true;
  }
}
