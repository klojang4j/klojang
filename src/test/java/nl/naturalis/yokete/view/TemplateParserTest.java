package nl.naturalis.yokete.view;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateParserTest {

  @Test
  public void parseVariables00() throws InvalidTemplateException {
    String src = "<tr><td>~%foo%</td></tr>";
    UnparsedPart p = new UnparsedPart(src, 0, src.length());
    List<Part> parts = TemplateParser.parseVariables(p, emptySet());
    assertEquals(3, parts.size());
    assertTrue(parts.get(0) instanceof UnparsedPart);
    assertEquals("<tr><td>", ((UnparsedPart) parts.get(0)).getContents());
    assertTrue(parts.get(1) instanceof VariablePart);
    VariablePart vp = (VariablePart) parts.get(1);
    assertEquals(EscapeType.NOT_SPECIFIED, vp.getEscapeType());
    assertEquals("foo", vp.getName());
    assertTrue(parts.get(2) instanceof UnparsedPart);
    assertEquals("</td></tr>", ((UnparsedPart) parts.get(2)).getContents());
  }

  @Test
  public void parseVariables01() throws InvalidTemplateException {
    String src = "<tr>\n<td>~%html:foo%</td>\n<!-- some comment -->\n<td>~%text:bar%</td>\n</tr>";
    UnparsedPart p = new UnparsedPart(src, 0, src.length());
    List<Part> parts = TemplateParser.parseVariables(p, emptySet());
    assertEquals(5, parts.size());
    assertTrue(parts.get(0) instanceof UnparsedPart);
    assertEquals("<tr>\n<td>", ((UnparsedPart) parts.get(0)).getContents());
    assertTrue(parts.get(1) instanceof VariablePart);
    VariablePart vp = (VariablePart) parts.get(1);
    assertEquals(EscapeType.ESCAPE_HTML, vp.getEscapeType());
    assertEquals("foo", vp.getName());
    assertTrue(parts.get(2) instanceof UnparsedPart);
    assertEquals("</td>\n<!-- some comment -->\n<td>", ((UnparsedPart) parts.get(2)).getContents());
    assertTrue(parts.get(3) instanceof VariablePart);
    vp = (VariablePart) parts.get(3);
    assertEquals(EscapeType.ESCAPE_NONE, vp.getEscapeType());
    assertEquals("bar", vp.getName());
    assertTrue(parts.get(4) instanceof UnparsedPart);
    assertEquals("</td>\n</tr>", ((UnparsedPart) parts.get(4)).getContents());
  }

  @Test
  public void parseNestedTemplates00() throws InvalidTemplateException {
    String src = IOMethods.toString(getClass(), "TemplateParserTest.parseNestedTemplates00.html");
    // System.out.println(src);
    TemplateParser parser = new TemplateParser(src, null);
    LinkedList<Part> parts = new LinkedList<>();
    Set<String> names = new HashSet<>();
    parser.parseNestedTemplates(src, parts, names);
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
  public void parseImportTemplates00() throws InvalidTemplateException {
    String src = IOMethods.toString(getClass(), "TemplateParserTest.parseImportedTemplates00.html");
    // System.out.println(src);
    UnparsedPart p = new UnparsedPart(src, 0, src.length());
    TemplateParser parser = new TemplateParser(src, getClass());
    Set<String> names = new HashSet<>();
    List<Part> parts = parser.parseImportedTemplates(p, names);
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
}
