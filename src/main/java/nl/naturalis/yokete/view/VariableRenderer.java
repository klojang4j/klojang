package nl.naturalis.yokete.view;

import java.util.function.BiFunction;

/**
 * A {@code VariableRenderer} is responsible for stringifying template variable values. It is not
 * responsible for escaping them properly.
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface VariableRenderer extends BiFunction<String, Object, String> {

  /**
   * Stringifies the variable with the specified name and value.
   *
   * @param varName The name of the variable whose value to stringify
   * @param value The value to stringify
   */
  @Override
  String apply(String varName, Object value);
}
