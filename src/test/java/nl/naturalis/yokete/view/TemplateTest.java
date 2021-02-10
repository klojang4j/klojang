package nl.naturalis.yokete.view;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateTest {

  @Test
  public void test00() {
    String tmpl = IOMethods.toString(getClass(), "test00.in.html");
    Template template = Template.parse(tmpl);
    List<Part> parts =
        template
            .getParts()
            .stream()
            .filter(VariablePart.class::isInstance)
            .collect(Collectors.toList());
    int varCount =
        (int) template.getParts().stream().filter(VariablePart.class::isInstance).count();
    assertEquals(11, varCount);
    assertEquals("topdeskId", parts.get(0).toString());
    assertEquals("id", parts.get(1).toString());
    assertEquals("amount", parts.get(9).toString());
    assertEquals("issueDate", parts.get(10).toString());
  }
}
