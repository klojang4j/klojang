package nl.naturalis.yokete.render;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import nl.naturalis.yokete.template.ParseException;
import nl.naturalis.yokete.template.Template;

public class RenderSessionTest {
  @Test
  public void test00() throws ParseException {
    Template t0 = Template.parse(getClass(), Path.of("RenderSessionTest.main.html"));
  }
}
