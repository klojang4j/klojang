package org.klojang.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.klojang.template.ParseException;
import org.klojang.template.Template;
import nl.naturalis.common.IOMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenderSessionTest01 {

  @Test
  public void test00() throws ParseException, RenderException {

    Template template = Template.fromResource(getClass(), "RenderSessionTest01.html");

    Map<String, Object> data = new HashMap<>();

    data.put("title", "Hello World!");

    Map<String, Object> company = new HashMap<>();
    data.put("company", company);

    company.put("director", "John Smith");

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

    RenderSession session = template.newRenderSession();
    session.insert(data);
    String actual = session.render();
    String expected = IOMethods.toString(getClass(), "RenderSessionTest01.expected.html");
    assertEquals(expected, actual);
  }
}
