package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gte;
import static nl.naturalis.common.check.CommonChecks.lt;

/**
 * Extension of {@code ArrayList} with a bit of sugar coating useful when reading SQL {@link
 * ResultSet result sets}. It basically lets you push the {@code ResultSet} out of the data access
 * layer into the REST/web layer without the latter actually acquiring an awkward dependency on
 * {@code java.sql}.
 *
 * @author Ayco Holleman
 */
public class QueryResult extends ArrayList<Row> {

  public static QueryResult empty() {
    return new QueryResult(0);
  }

  QueryResult() {
    super();
  }

  QueryResult(Collection<? extends Row> c) {
    super(c);
  }

  QueryResult(int initialCapacity) {
    super(initialCapacity);
  }

  public <T> T get(int rowNum, String colName) {
    Check.that(rowNum, "rowNum").is(gte(), 0).is(lt(), size());
    return get(rowNum).valueOf(colName);
  }

  public String getString(int rowNum, String colName) {
    Check.that(rowNum, "rowNum").is(gte(), 0).is(lt(), size());
    return get(rowNum).getString(colName);
  }

  public int getInt(int rowNum, String colName) {
    Check.that(rowNum, "rowNum").is(gte(), 0).is(lt(), size());
    return get(rowNum).getInt(colName);
  }

  public int getInt(int rowNum, String colName, int defaultValue) {
    Check.that(rowNum, "rowNum").is(gte(), 0).is(lt(), size());
    return get(rowNum).getInt(colName, defaultValue);
  }
}
