package nl.naturalis.yokete.view;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;
import static org.junit.jupiter.api.Assertions.*;
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
  public void test06() {
    String s = IOMethods.toString(getClass(), "RegexTest.test06.html");
    assertTrue(REGEX_HIDDEN_TMPL.matcher(s).find());
  }

  @Test
  public void test07() {
    assertTrue(REGEX_IMPORT.matcher("~%%import:/views/rows.html%").find());
  }

  @Test
  public void test08() {
    assertTrue(REGEX_IMPORT.matcher("~%%import:foo:/views/rows.html%").find());
  }

  @Test
  public void test09() {
    assertTrue(REGEX_HIDDEN_IMPORT.matcher("FOO<!-- ~%%import:/views/rows.html% -->BAR").find());
  }

  @Test
  public void test10() {
    assertTrue(
        REGEX_HIDDEN_IMPORT
            .matcher("FOO\n<!-- \t ~%%import:foo:/views/rows.html%\n\n--> BAR")
            .find());
  }
}
