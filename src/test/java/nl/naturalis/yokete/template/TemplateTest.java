package nl.naturalis.yokete.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.yokete.render.Page;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.render.RenderSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static nl.naturalis.common.StringMethods.append;

public class TemplateTest {

  @Test
  public void test00() throws ParseException {
    Template t0 = Template.parseResource(getClass(), "TemplateTest.main.html");
    assertEquals(0, t0.countVariables());
    assertEquals(1, t0.countNestedTemplates());
    assertEquals("company", t0.getNestedTemplates().iterator().next().getName());
    Template t0_0 = t0.getNestedTemplate("company");
    assertEquals(5, t0_0.countVariables());
    assertEquals(1, t0_0.countNestedTemplates());
    assertEquals(
        List.of("name", "poBox", "established", "director"), List.copyOf(t0_0.getVariables()));
    Template t0_0_0 = t0_0.getNestedTemplate("departments");
    assertEquals(2, t0_0_0.countVariables());
    assertEquals(1, t0_0_0.countNestedTemplates());
    assertEquals(List.of("name", "managerName"), List.copyOf(t0_0_0.getVariables()));
    Template t0_0_0_0 = t0_0_0.getNestedTemplate("employees");
    assertEquals(2, t0_0_0_0.countVariables());
    assertEquals(1, t0_0_0_0.countNestedTemplates());
    assertEquals(List.of("name", "age"), List.copyOf(t0_0_0_0.getVariables()));
    Template t0_0_0_0_0 = t0_0_0_0.getNestedTemplate("roles");
    assertEquals(1, t0_0_0_0_0.countVariables());
    assertEquals(0, t0_0_0_0_0.countNestedTemplates());
    assertEquals(List.of("role"), List.copyOf(t0_0_0_0_0.getVariables()));
  }

  @Test // getVariableNamesPerTemplate
  public void test01() throws ParseException {
    Template t0 = Template.parseResource(getClass(), "TemplateTest.main.html");
    String expected =
        "company:name;company:poBox;company:established;company:director;"
            + "departments:name;departments:managerName;"
            + "employees:name;employees:age;"
            + "roles:role;";
    StringBuilder sb = new StringBuilder(100);
    TemplateUtils.getVarsPerTemplate(t0)
        .forEach(t -> append(sb, t.getLeft().getName(), ":", t.getRight(), ";"));
    assertEquals(expected, sb.toString());
  }

  @Test // getVarPartIndices
  public void test02() throws ParseException {
    Template t0 = Template.parseResource(getClass(), "TemplateTest.main.html");
    IntList indices = t0.getNestedTemplate("company").getVarPartIndices().get("name");
    assertEquals(2, indices.size());
    indices = t0.getNestedTemplate("company").getVarPartIndices().get("poBox");
    assertEquals(1, indices.size());
  }

  @Test // getVarPartIndices
  public void test03() throws ParseException {
    Template t0 = Template.parseResource(getClass(), "TemplateTest.main.html");
    TemplateUtils.getNestedTemplatesRecursive(t0)
        .forEach(t -> System.out.println(TemplateUtils.getFQName(t, "test")));
  }

  @Test // getNestedTemplates
  public void test04() throws ParseException {
    Template t0 = Template.parseResource(getClass(), "TemplateTest.main.html");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
    t0 = t0.getNestedTemplate("company");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
    t0 = t0.getNestedTemplate("departments");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
    t0 = t0.getNestedTemplate("employees");
    t0.getNestedTemplates().forEach(t -> System.out.println(t.getName()));
  }

  @Test
  public void testEncounterOrder() throws ParseException {
    Template t0 = Template.parseResource(getClass(), "TemplateTest.testEncounterOrder.html");
    String varStr =
        "topdeskId, id, department, instituteCode, collectionCode, firstNumber, lastNumber, amount, issueDate";
    String[] varNames = varStr.split(",");
    List<String> expected = Arrays.stream(varNames).map(String::strip).collect(Collectors.toList());
    List<String> actual = new ArrayList<>(t0.getVariables());
    assertEquals(expected, actual);
  }

  @Test
  public void testAdjacentVars01() throws ParseException, RenderException {
    String s = "~%var1%~%var2%";
    Template t0 = Template.parseString(s);
    assertEquals(2, t0.getParts().size());
    RenderSession rs = Page.configure(t0).newRenderSession();
    rs.set("var2", "Bar");
    rs.set("var1", "Foo");
    assertEquals("FooBar", rs.render());
  }

  @Test
  public void testAdjacentVars02() throws ParseException, RenderException {
    String s = " ~%var1%~%var2% ";
    Template t0 = Template.parseString(s);
    assertEquals(4, t0.getParts().size());
    RenderSession rs = Page.configure(t0).newRenderSession();
    rs.set("var2", "Bar");
    rs.set("var1", "Foo");
    assertEquals(" FooBar ", rs.render());
  }

  @Test
  public void testAdjacentVars03() throws ParseException, RenderException {
    String s = "~%var1%%~%var2%";
    Template t0 = Template.parseString(s);
    assertEquals(3, t0.getParts().size());
    RenderSession rs = Page.configure(t0).newRenderSession();
    rs.set("var2", "Bar");
    rs.set("var1", "Foo");
    assertEquals("Foo%Bar", rs.render());
  }

  @Test
  public void testAdjacentVars04() throws ParseException, RenderException {
    String s = "~%var1%~~%var2%";
    Template t0 = Template.parseString(s);
    assertEquals(3, t0.getParts().size());
    RenderSession rs = Page.configure(t0).newRenderSession();
    rs.set("var2", "Bar");
    rs.set("var1", "Foo");
    assertEquals("Foo~Bar", rs.render());
  }
}
