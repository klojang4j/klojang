package org.klojang.util.namemappers;

import org.klojang.render.NameMapper;
import nl.naturalis.common.check.Check;
import static java.lang.Character.toUpperCase;
import static nl.naturalis.common.check.CommonChecks.empty;

/**
 * Converts camel case identifiers to word case identifiers. For example {@code myBloodyValentine}
 * becomes {@code MyBloodyValentine}.
 *
 * @author Ayco Holleman
 */
public class CamelCaseToWordCase implements NameMapper {

  /**
   * Returns an instance of {@code CamelCaseToWordCase}.
   *
   * @return An instance of {@code CamelCaseToWordCase}
   */
  public static CamelCaseToWordCase camelCaseToWordCase() {
    return new CamelCaseToWordCase();
  }

  @Override
  public String map(String name) {
    Check.that(name, "name").isNot(empty());
    return toUpperCase(name.charAt(0)) + name.substring(1);
  }
}
