package nl.naturalis.yokete.view;

import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateTest {

  @Test
  public void test00() {
    String tmpl = IOMethods.toString(getClass(), "test00.in.html");
    Template template = new Template(tmpl);
    assertEquals(11, template.countVariables());
    assertEquals("topdeskId", template.getVariables().get(0));
    assertEquals("id", template.getVariables().get(1));
    assertEquals("amount", template.getVariables().get(9));
    assertEquals("issueDate", template.getVariables().get(10));
  }
}
