package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static nl.naturalis.common.StringMethods.append;

public class TemplateTest {

  @Test
  public void test00() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("TemplateTest.main.html"));
    assertEquals(0, t0.countVariables());
    assertEquals(1, t0.countNestedTemplates());
    assertEquals("company", t0.getNestedTemplates().iterator().next().getName());
    Template t0_0 = t0.getNestedTemplate("company");
    assertEquals(4, t0_0.countVariables());
    assertEquals(1, t0_0.countNestedTemplates());
    assertEquals(
        List.of("name", "poBox", "established", "director"), List.copyOf(t0_0.getVariableNames()));
    Template t0_0_0 = t0_0.getNestedTemplate("departments");
    assertEquals(2, t0_0_0.countVariables());
    assertEquals(1, t0_0_0.countNestedTemplates());
    assertEquals(List.of("name", "managerName"), List.copyOf(t0_0_0.getVariableNames()));
    Template t0_0_0_0 = t0_0_0.getNestedTemplate("employees");
    assertEquals(2, t0_0_0_0.countVariables());
    assertEquals(1, t0_0_0_0.countNestedTemplates());
    assertEquals(List.of("name", "age"), List.copyOf(t0_0_0_0.getVariableNames()));
    Template t0_0_0_0_0 = t0_0_0_0.getNestedTemplate("roles");
    assertEquals(1, t0_0_0_0_0.countVariables());
    assertEquals(0, t0_0_0_0_0.countNestedTemplates());
    assertEquals(List.of("role"), List.copyOf(t0_0_0_0_0.getVariableNames()));
  }

  @Test // getVariableNamesPerTemplate
  public void test01() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("TemplateTest.main.html"));
    String expected =
        "company:name;company:poBox;company:established;company:director;"
            + "departments:name;departments:managerName;"
            + "employees:name;employees:age;"
            + "roles:role;";
    StringBuilder sb = new StringBuilder(100);
    t0.getVariableNamesPerTemplate()
        .forEach(t -> append(sb, t.getLeft().getName(), ":", t.getRight(), ";"));
    assertEquals(expected, sb.toString());
  }

  @Test
  public void test02() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("TemplateTest.main.html"));
  }
}
