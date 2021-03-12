package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.collection.IntList;
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
    assertEquals(5, t0_0.countVariables());
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

  @Test // getVarPartIndices
  public void test02() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("TemplateTest.main.html"));
    IntList indices = t0.getNestedTemplate("company").getVarPartIndices().get("name");
    assertEquals(2, indices.size());
    indices = t0.getNestedTemplate("company").getVarPartIndices().get("poBox");
    assertEquals(1, indices.size());
  }

  @Test // getVarPartIndices
  public void test03() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("TemplateTest.main.html"));
    t0.getNestedTemplatesRecursive()
        .forEach(t -> System.out.println(TemplateUtils.getFQName(t, "test")));
  }

  @Test // getNestedTemplates
  public void test04() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("TemplateTest.main.html"));
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
    t0 = t0.getNestedTemplate("company");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
    t0 = t0.getNestedTemplate("departments");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
    t0 = t0.getNestedTemplate("employees");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
  }
}
