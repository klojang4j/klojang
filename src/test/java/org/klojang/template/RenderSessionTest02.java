package org.klojang.template;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import nl.naturalis.common.IOMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenderSessionTest02 {

  @Test
  public void test00_a() throws ParseException, RenderException {
    List<Employee> employees = getEmployees();
    Employee manager = getManager();
    Department department = new Department("HR", manager, employees);
    Template template = Template.fromResource(getClass(),
        "RenderSessionTest02_a.txt");
    RenderSession session = template.newRenderSession();
    session.insert(department);
    // session.render(System.out);
    String actual = session.render();
    String expected = IOMethods.getContents(getClass(),
        "RenderSessionTest02.expected.txt");
    assertEquals(expected, actual);
  }

  @Test
  public void test00_b() throws ParseException, RenderException {
    List<Employee> employees = getEmployees();
    Employee manager = getManager();
    Department department = new Department("HR", manager, employees);
    Template template = Template.fromResource(getClass(),
        "RenderSessionTest02_b.txt");
    RenderSession session = template.newRenderSession();
    session.insert(department);
    // session.render(System.out);
    String actual = session.render();
    String expected = IOMethods.getContents(getClass(),
        "RenderSessionTest02.expected.txt");
    assertEquals(expected, actual);
  }

  private static List<Employee> getEmployees() {
    return List.of(
        new Employee(
            "Richard Dawkins",
            new Address("Dawkins street", 8),
            LocalDate.of(1978, 8, 12)),
        new Employee("Roger Penrose",
            new Address("Penrose blvd.", 103),
            LocalDate.of(1989, 2, 17)),
        new Employee(
            "Juan Maldacena",
            new Address("Maldacena ave.", 34),
            LocalDate.of(1961, 10, 29)));
  }

  private static Employee getManager() {
    return new Employee(
        "Albert Einstein",
        new Address("Einstein rd.", 2),
        LocalDate.of(1968, 7, 12));
  }

  public static class Address {

    String street;
    int number;

    public Address(String street, int number) {
      this.street = street;
      this.number = number;
    }

    public String getStreet() {
      return street;
    }

    public int getNumber() {
      return number;
    }

  }

  public static class Employee {

    String name;
    Address address;
    LocalDate birthDate;

    public Employee(String name, Address address, LocalDate birthDate) {
      this.name = name;
      this.address = address;
      this.birthDate = birthDate;
    }

    public String getName() {
      return name;
    }

    public Address getAddress() {
      return address;
    }

    public LocalDate getBirthDate() {
      return birthDate;
    }

  }

  public static class Department {

    String name;
    Employee manager;
    List<Employee> employees;

    public Department(String name, Employee manager, List<Employee> employees) {
      this.name = name;
      this.manager = manager;
      this.employees = employees;
    }

    public String getName() {
      return name;
    }

    public Employee getManager() {
      return manager;
    }

    public List<Employee> getEmployees() {
      return employees;
    }

  }

}
