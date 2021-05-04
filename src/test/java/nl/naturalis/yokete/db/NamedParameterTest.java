package nl.naturalis.yokete.db;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NamedParameterTest {

  @Test
  public void test00() {
    String s = "SELECT FOO FROM BAR WHERE FULL_NAME = :fullName";
    SQL sql = SQL.create(s);
    List<NamedParameter> params = sql.getParameters();
    assertEquals(1, params.size());
    assertEquals("fullName", params.get(0).getName());
    assertEquals(1, params.get(0).getIndices().length);
    assertEquals(1, params.get(0).getIndices()[0]);
    assertEquals("SELECT FOO FROM BAR WHERE FULL_NAME = ?", sql.getNormalizedSQL());
  }

  @Test
  public void test01() {
    String s =
        "SELECT FOO FROM BAR WHERE FULL_NAME = :name "
            + "AND LAST_NAME = :lastName OR LAST_NAME = :name "
            + "LIMIT :from,:to";
    SQL sql = SQL.create(s);
    Map<String, int[]> paramMap = sql.getParameterMap();
    assertEquals(4, paramMap.size());
    assertArrayEquals(new int[] {1, 3}, paramMap.get("name"));
    assertArrayEquals(new int[] {2}, paramMap.get("lastName"));
    assertArrayEquals(new int[] {4}, paramMap.get("from"));
    assertArrayEquals(new int[] {5}, paramMap.get("to"));
    List<NamedParameter> params = sql.getParameters();
    assertEquals(4, params.size());
    assertEquals("name", params.get(0).getName());
    assertEquals("lastName", params.get(1).getName());
    assertEquals("from", params.get(2).getName());
    assertEquals("to", params.get(3).getName());
    assertArrayEquals(new int[] {1, 3}, params.get(0).getIndices());
  }
}
