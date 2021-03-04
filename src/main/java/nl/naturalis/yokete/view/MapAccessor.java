package nl.naturalis.yokete.view;

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
public class MapAccessor implements Accessor<Map<String, Object>> {

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
  public Object getValue(Map<String, Object> from, String varName) throws RenderException {
    Check.notNull(from, "from");
    String key = Check.notNull(varName, "varName").ok(mapper::apply);
    return from.getOrDefault(key, ABSENT);
  }
}
