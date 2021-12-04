package org.klojang.template;

import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.klojang.template.Regex.*;

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
    assertTrue(REGEX_VARIABLE_CMT.matcher("<!--~%person%-->").find());
    assertTrue(REGEX_VARIABLE_CMT.matcher("<!-- ~%person% -->").find());
    assertTrue(REGEX_VARIABLE_CMT.matcher("<!--\t~%person%\t-->").find());
    assertTrue(REGEX_VARIABLE_CMT.matcher("FOO\t<!--~%person%-->BAR").find());
    assertTrue(REGEX_VARIABLE_CMT.matcher("\n<!--      \t~%person%\t\t   -->\n").find());
  }

  @Test
  public void test04() {
    String s = IOMethods.toString(getClass(), "RegexTest.test04.html");
    assertTrue(REGEX_INLINE_TMPL.matcher(s).find());
  }

  @Test
  public void test05() {
    String s = IOMethods.toString(getClass(), "RegexTest.test05.html");
    assertTrue(REGEX_INLINE_TMPL_CMT.matcher(s).find());
  }

  @Test
  public void include01() {
    String s = IOMethods.toString(getClass(), "RegexTest.test06.html");
    assertTrue(REGEX_INLINE_TMPL_CMT.matcher(s).find());
  }

  @Test
  public void include02() {
    assertTrue(REGEX_INCLUDED_TMPL.matcher("~%%include:/views/rows.html%").find());
  }

  @Test
  public void include03() {
    assertTrue(REGEX_INCLUDED_TMPL.matcher("~%%include:foo:/views/rows.html%").find());
  }

  @Test
  public void include04() {
    assertTrue(
        REGEX_INCLUDED_TMPL_CMT.matcher("FOO<!-- ~%%include:/views/rows.html% -->BAR").find());
  }

  @Test
  public void include05() {
    assertTrue(
        REGEX_INCLUDED_TMPL_CMT
            .matcher("FOO\n<!-- \t ~%%include:foo:/views/rows.html%\t--> BAR")
            .find());
  }

  @Test
  public void include06() {
    Matcher m =
        REGEX_INCLUDED_TMPL.matcher("FOO ******* ~%%include:foo:/views/rows.html% ******* BAR");
    m.find();
    assertEquals(3, m.groupCount()); // The match itself, group(0), does not count
    assertEquals("~%%include:foo:/views/rows.html%", m.group(0));
    assertEquals("foo:", m.group(1));
    assertEquals("foo", m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }

  @Test
  public void include07() {
    Matcher m = REGEX_INCLUDED_TMPL.matcher("FOO ******* ~%%include:/views/rows.html% ******* BAR");
    m.find();
    assertEquals(3, m.groupCount()); // Number of groups defined by regex, not by input
    assertEquals("~%%include:/views/rows.html%", m.group(0));
    assertNull(m.group(1));
    assertNull(m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }

  @Test
  public void hiddenInclude01() {
    Matcher m =
        REGEX_INCLUDED_TMPL_CMT.matcher(
            "FOO ******* <!--~%%include:/views/rows.html%--> ******* BAR");
    m.find();
    assertEquals(3, m.groupCount()); // Number of groups defined by regex, not by input
    assertEquals("<!--~%%include:/views/rows.html%-->", m.group(0));
    assertNull(m.group(1));
    assertNull(m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }

  @Test
  public void hiddenInclude02() {
    Matcher m =
        REGEX_INCLUDED_TMPL_CMT.matcher(
            "FOO ******* <!--\n\t~%%include:/views/rows.html% \t\n  --> ******* BAR");
    m.find();
    assertEquals(3, m.groupCount()); // Number of groups defined by regex, not by input
    assertEquals("<!--\n\t~%%include:/views/rows.html% \t\n  -->", m.group(0));
    assertNull(m.group(1));
    assertNull(m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }

  @Test
  public void hiddenInclude03() {
    Matcher m =
        REGEX_INCLUDED_TMPL_CMT.matcher(
            "\n\nFOO ******* <!--\n\t~%%include:/views/rows.html% \t\n  --> ******* \nBAR");
    m.find();
    assertEquals(3, m.groupCount()); // Number of groups defined by regex, not by input
    assertEquals("<!--\n\t~%%include:/views/rows.html% \t\n  -->", m.group(0));
    assertNull(m.group(1));
    assertNull(m.group(2));
    assertEquals("/views/rows.html", m.group(3));
  }

  @Test
  public void ditch00() {
    Matcher m = REGEX_DITCH_BLOCK.matcher("FOO <!--%%--><tr><td>Hi!</td></tr><!--%%-->");
    assertTrue(m.find());
  }

  @Test
  public void ditch01() {
    Matcher m = REGEX_DITCH_TOKEN.matcher("<!--%%--><!-- Single-line ditch block --><!--%%-->");
    assertTrue(m.find());
    assertTrue(m.find());
  }

  @Test
  public void ditch02() {
    Matcher m = REGEX_DITCH_TOKEN.matcher("Foo\n<!--%%-->Multi-line ditch block\n<!--%%-->BAR");
    assertTrue(m.find());
    assertTrue(m.find());
  }

  @Test
  public void ditch03() {
    String s = IOMethods.toString(getClass(), "RegexTest.ditch03.html");
    Matcher m = REGEX_DITCH_BLOCK.matcher(s);
    assertTrue(m.find());
    assertTrue(m.find());
    assertTrue(m.find());
    assertFalse(m.find());
  }

  @Test
  public void ditch04() {
    String s = IOMethods.toString(getClass(), "RegexTest.ditch04.html");
    Matcher m = REGEX_DITCH_BLOCK.matcher(s);
    assertTrue(m.find());
    assertTrue(m.find());
    assertTrue(m.find());
    assertFalse(m.find());
  }

  @Test
  public void hiddenVar01() {
    Matcher m = REGEX_VARIABLE_CMT.matcher("<!-- ~%person% -->");
    assertTrue(m.find());
    assertEquals("person", m.group(3));
  }
}
