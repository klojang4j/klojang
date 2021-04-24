package nl.naturalis.yokete.util;

import java.sql.Types;
import org.junit.jupiter.api.Test;
import nl.naturalis.yokete.db.SQLTypeNames;
import static org.junit.jupiter.api.Assertions.*;

public class SQLTypeNamesTest {

  @Test
  public void test00() {
    assertEquals("VARCHAR", SQLTypeNames.getTypeName(Types.VARCHAR));
    assertEquals("INTEGER", SQLTypeNames.getTypeName(Types.INTEGER));
    assertEquals("TIMESTAMP", SQLTypeNames.getTypeName(Types.TIMESTAMP));
    assertEquals("TINYINT", SQLTypeNames.getTypeName(Types.TINYINT));
  }

  @Test()
  public void test01() {
    assertThrows(IllegalArgumentException.class, () -> SQLTypeNames.getTypeName(-17));
  }

  @Test
  public void test02() {
    // Just call to make sure we can
    SQLTypeNames.printAll(System.out);
  }
}
