package nl.naturalis.yokete.render;

import java.util.Map;
import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;

/**
 * A simple map-based {@code Accessor} implementation. The {@code Map} passed to the {@link
 * #getValue(Map, String) getValue} method is assumed to be a simple, two-dimensional key-value
 * store without nested structures.
 *
 * @author Ayco Holleman
 */
public class MapAccessor implements Accessor {

  private final UnaryOperator<String> mapper;

  /**
   * Creates a {@code KeyValueAccessor} that assumes a one-to-once correspondence between template
   * variable names and map keys.
   */
  public MapAccessor() {
    this.mapper = x -> x;
  }

  /**
   * Creates a {@code KeyValueAccessor} that translates template variable names using the specified
   * operator.
   *
   * @param nameMapper
   */
  public MapAccessor(UnaryOperator<String> nameMapper) {
    this.mapper = Check.notNull(nameMapper).ok();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getValue(Object from, String varName) throws RenderException {
    Check.notNull(from, "from");
    String key = Check.notNull(varName, "varName").ok(mapper::apply);
    Map<String, Object> map;
    try {
      map = (Map<String, Object>) from;
    } catch (ClassCastException e) {
      throw RenderException.unexpectedType(varName, from);
    }
    return map.getOrDefault(key, ABSENT);
  }

  @Override
  public Accessor getAccessorForNestedTemplate(String tmplName) {
    return this;
  }
}
