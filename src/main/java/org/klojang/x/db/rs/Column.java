package org.klojang.x.db.rs;

/**
 * Only used internally to construct a Row instance. We really could have used the Tuple class or
 * Map.Entry or whatever else just as well. But that would have exposed the Row.withColumns(...)
 * factory method to the outside world. It's still exposed of course because it's a public method in
 * an exported package. But at least the outside world can't call it because the Column class is not
 * exported. If only Java were to introduce a module-private access modifier ...
 *
 * @author Ayco Holleman
 */
public class Column {

  private String name;
  private Object value;

  Column(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }
}
