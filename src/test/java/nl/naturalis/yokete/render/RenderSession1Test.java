package nl.naturalis.yokete.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import nl.naturalis.yokete.accessors.MapAccessor;
import nl.naturalis.yokete.template.ParseException;
import nl.naturalis.yokete.template.Template;

public class RenderSession1Test {

  @Test
  public void test00() throws ParseException, RenderException {

    Template tmpl = Template.parseResource(getClass(), "RenderSession1Test.html");
    TemplateStringifiers sf = TemplateStringifiers.SIMPLE_STRINGIFIER;
    Page page = Page.configure(tmpl, (t, u) -> new MapAccessor(), sf);

    Map<String, Object> data = new HashMap<>();

    data.put("title", "Hello World!");

    Map<String, Object> company = new HashMap<>();
    data.put("company", company);

    company.put("directory", "John Smith");

    List<Map<String, Object>> depts = new ArrayList<>();
    company.put("departments", depts);

    Map<String, Object> dept = new HashMap<>();
    dept.put("name", "HR");
    dept.put("manager", "Kristina Aguilera");
    depts.add(dept);

    dept = new HashMap<>();
    dept.put("name", "ICT");
    dept.put("manager", "Woody Harrelson");
    depts.add(dept);

    dept = new HashMap<>();
    dept.put("name", "Sales");
    dept.put("manager", "Robert de Niro");
    depts.add(dept);

    RenderSession session = page.newRenderSession();
    session.insert(data);

    session.render(System.out);
  }
}
