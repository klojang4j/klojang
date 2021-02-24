package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import nl.naturalis.yokete.view.EscapeType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateParserTest {

  @Test
  public void parseVariables00() throws ParseException {
    String src = "<tr><td>~%foo%</td></tr>";
    Template template = Template.parse(src);
    // System.out.println(template);
    List<Part> parts = template.getParts();
    assertEquals(3, parts.size());
    assertTrue(parts.get(0) instanceof TextPart);
    assertEquals("<tr><td>", ((TextPart) parts.get(0)).getText());
    assertTrue(parts.get(1) instanceof VariablePart);
    VariablePart vp = (VariablePart) parts.get(1);
    assertEquals(EscapeType.NOT_SPECIFIED, vp.getEscapeType());
    assertEquals("foo", vp.getName());
    assertTrue(parts.get(2) instanceof TextPart);
    assertEquals("</td></tr>", ((TextPart) parts.get(2)).getText());
  }

  @Test
  public void parseVariables01() throws ParseException {
    String src = "<tr>\n<td>~%html:foo%</td>\n<!-- some comment -->\n<td>~%text:bar%</td>\n</tr>";
    Template template = Template.parse(getClass(), src);
    // System.out.println(template);
    List<Part> parts = template.getParts();
    assertEquals(5, parts.size());
    assertTrue(parts.get(0) instanceof TextPart);
    assertEquals("<tr>\n<td>", ((TextPart) parts.get(0)).getText());
    assertTrue(parts.get(1) instanceof VariablePart);
    VariablePart vp = (VariablePart) parts.get(1);
    assertEquals(EscapeType.ESCAPE_HTML, vp.getEscapeType());
    assertEquals("foo", vp.getName());
    assertTrue(parts.get(2) instanceof TextPart);
    assertEquals("</td>\n<!-- some comment -->\n<td>", ((TextPart) parts.get(2)).getText());
    assertTrue(parts.get(3) instanceof VariablePart);
    vp = (VariablePart) parts.get(3);
    assertEquals(EscapeType.ESCAPE_NONE, vp.getEscapeType());
    assertEquals("bar", vp.getName());
    assertTrue(parts.get(4) instanceof TextPart);
    assertEquals("</td>\n</tr>", ((TextPart) parts.get(4)).getText());
  }

  @Test
  public void parseNestedTemplates00() throws ParseException {
    Path path = Path.of("TemplateParserTest.parseNestedTemplates00.html");
    Template template = Template.parse(getClass(), path);
    // System.out.println(template);
    List<Part> parts = template.getParts();
    assertTrue(parts.get(1) instanceof TemplatePart);
    Template t = ((TemplatePart) parts.get(1)).getTemplate();
    assertEquals(2, t.getAllNames().size());
    assertTrue(t.getAllNames().contains("selectedName"));
    assertTrue(t.getAllNames().contains("selectedAge"));
    assertTrue(parts.get(3) instanceof TemplatePart);
    t = ((TemplatePart) parts.get(3)).getTemplate();
    assertEquals(2, t.getAllNames().size());
    assertTrue(t.getAllNames().contains("name"));
    assertTrue(t.getAllNames().contains("age"));
  }

  @Test
  public void parseIncludedTemplates00() throws ParseException {
    Path path = Path.of("TemplateParserTest.parseIncludedTemplates00.html");
    Template template = Template.parse(getClass(), path);
    // System.out.println(template);
    List<Part> parts = template.getParts();
    assertTrue(parts.get(1) instanceof TemplatePart);
    TemplatePart tp = (TemplatePart) parts.get(1);
    assertEquals("jsVars", tp.getName());
    Template t = tp.getTemplate();
    assertEquals(2, t.getAllNames().size());
    assertTrue(t.getAllNames().contains("selectedName"));
    assertTrue(t.getAllNames().contains("selectedAge"));
    assertTrue(parts.get(3) instanceof TemplatePart);
    tp = (TemplatePart) parts.get(3);
    assertEquals("tableRow", tp.getName());
    t = tp.getTemplate();
    assertEquals(2, t.getAllNames().size());
    assertTrue(t.getAllNames().contains("name"));
    assertTrue(t.getAllNames().contains("age"));
  }

  public void testDitchBlock00() {}
}
