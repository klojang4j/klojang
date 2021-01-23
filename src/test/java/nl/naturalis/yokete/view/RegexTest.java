package nl.naturalis.yokete.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static nl.naturalis.yokete.view.Regex.REGEX_VARIABLE;

public class RegexTest {

  @Test
  public void test00() {
    assertTrue(REGEX_VARIABLE.matcher("~%person%").find());
    assertTrue(REGEX_VARIABLE.matcher("foo~%person%").find());
    assertTrue(REGEX_VARIABLE.matcher("foo ~%person%").find());
    assertTrue(REGEX_VARIABLE.matcher("foo ~%person%bar").find());
    assertTrue(REGEX_VARIABLE.matcher("foo ~%person% bar").find());
    assertTrue(REGEX_VARIABLE.matcher("foo ~%person% bar").find());
    assertTrue(REGEX_VARIABLE.matcher("foo\n~%person%\nbar").find());
  }

  @Test
  public void test01() {
    assertTrue(REGEX_VARIABLE.matcher("~%person.address%").find());
    assertTrue(REGEX_VARIABLE.matcher("~%person.address.street%").find());
  }

  @Test
  public void test02() {
    assertTrue(REGEX_VARIABLE.matcher("~%html:person.address%").find());
    assertTrue(REGEX_VARIABLE.matcher("~%js:person.address%").find());
    assertTrue(REGEX_VARIABLE.matcher("~%text:person.address%").find());
  }
}
