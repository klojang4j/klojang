package nl.naturalis.yokete.view;

import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static nl.naturalis.yokete.view.Regex.*;

public class RegexTest {

  @Test
  public void print() {
    Regex.printAll();
  }

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

  @Test
  public void test03() {
    assertTrue(REGEX_HIDDEN_VAR.matcher("<!--~%person%-->").find());
    assertTrue(REGEX_HIDDEN_VAR.matcher("<!-- ~%person% -->").find());
    assertTrue(REGEX_HIDDEN_VAR.matcher("<!--\t~%person%\t-->").find());
    assertTrue(REGEX_HIDDEN_VAR.matcher("foo\n<!--~%person%-->bar").find());
    assertTrue(REGEX_HIDDEN_VAR.matcher("<!--      \n~%person%\n\n   -->").find());
  }

  @Test
  public void test04() {
    String s = IOMethods.toString(getClass(), "RegexTest.test04.html");
    assertTrue(REGEX_TEMPLATE.matcher(s).find());
  }

  @Test
  public void test05() {
    String s = IOMethods.toString(getClass(), "RegexTest.test05.html");
    assertTrue(REGEX_HIDDEN_TMPL.matcher(s).find());
  }

  @Test
  public void import01() {
    String s = IOMethods.toString(getClass(), "RegexTest.test06.html");
    assertTrue(REGEX_HIDDEN_TMPL.matcher(s).find());
  }

  @Test
  public void import02() {
    assertTrue(REGEX_INCLUDE.matcher("~%%include:/views/rows.html%").find());
  }

  @Test
  public void import03() {
    assertTrue(REGEX_INCLUDE.matcher("~%%include:foo:/views/rows.html%").find());
  }

  @Test
  public void import04() {
    assertTrue(REGEX_HIDDEN_INCLUDE.matcher("FOO<!-- ~%%include:/views/rows.html% -->BAR").find());
  }

  @Test
  public void import05() {
    assertTrue(
        REGEX_HIDDEN_INCLUDE
            .matcher("FOO\n<!-- \t ~%%include:foo:/views/rows.html%\n\n--> BAR")
            .find());
  }

  @Test
  public void import06() {
    Matcher m = REGEX_INCLUDE.matcher("FOO ******* ~%%include:foo:/views/rows.html% ******* BAR");
    m.find();
    assertEquals(3, m.groupCount()); // The match itself, group(0), does not count
    assertEquals("~%%include:foo:/views/rows.html%", m.group(0));
    assertEquals("foo:", m.group(1));
    assertEquals("foo", m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }

  @Test
  public void import07() {
    Matcher m = REGEX_INCLUDE.matcher("FOO ******* ~%%include:/views/rows.html% ******* BAR");
    m.find();
    assertEquals(3, m.groupCount()); // Number of groups defined by regex, not by input
    assertEquals("~%%include:/views/rows.html%", m.group(0));
    assertNull(m.group(1));
    assertNull(m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }
}
