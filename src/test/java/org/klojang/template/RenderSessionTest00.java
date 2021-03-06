package org.klojang.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenderSessionTest00 {

  @Test
  public void test00() throws ParseException, RenderException {

    Template template = Template.fromResource(getClass(),
        "RenderSessionTest00.html");

    Map<String, Object> data = new HashMap<>();
    Map<String, Object> company = new HashMap<>();
    data.put("company", company);

    company.put("name", "Shell");
    company.put("poBox", "AB12345");
    company.put("established", "1932-04-04");
    company.put("directory", "John Smith");

    Map<String, Object> dept0 = new HashMap<>();
    dept0.put("name", "HR");
    dept0.put("managerName", "Kristina Aguilera");
    List<Map<String, Object>> emps0 = new ArrayList<>();
    dept0.put("employees", emps0);

    Map<String, Object> dept1 = new HashMap<>();
    dept1.put("name", "ICT");
    dept1.put("managerName", "Woody Harrelson");
    List<Map<String, Object>> emps1 = new ArrayList<>();
    dept1.put("employees", emps1);

    company.put("departments", List.of(dept0, dept1));

    Map<String, Object> emp = new HashMap<>();
    emp.put("name", "John Travolta");
    emp.put("sex", "M");
    emp.put("birthDate", "1963-03-03");
    emps0.add(emp);

    emp = new HashMap<>();
    emp.put("name", "Tony Chocolonely");
    emp.put("sex", "M");
    emp.put("birthDate", "1972-04-04");
    emps0.add(emp);

    emp = new HashMap<>();
    emp.put("name", "Queen Elisabeth");
    emp.put("sex", "F");
    emp.put("birthDate", "1922-01-02");
    emps0.add(emp);

    emp = new HashMap<>();
    emp.put("name", "George Clooney");
    emp.put("sex", "M");
    emp.put("birthDate", "1962-01-02");
    emps1.add(emp);

    emp = new HashMap<>();
    emp.put("name", "Joe Biden");
    emp.put("sex", "M");
    emp.put("birthDate", "1942-08-02");
    emps1.add(emp);

    RenderSession session = template.newRenderSession();
    session.insert(data);
    // session.render(System.out);
    String actual = session.render();
    String expected = IOMethods.getContents(getClass(),
        "RenderSessionTest00.expected.html");
    assertEquals(expected, actual);
  }

}
