package org.klojang.util.namemappers;

import org.klojang.template.NameMapper;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.empty;
import static java.lang.Character.*;

/**
 * Converts word case identifiers to came cal identifiers. For exampl {@code MyBloodyValentine}
 * becomes {@code myBloodyValentine}.
 *
 * @author Ayco Holleman
 */
public class WordCaseToCameCase implements NameMapper {

  /**
   * Returns an instance of {@code WordCaseToCameCase}.
   *
   * @return An instance of {@code WordCaseToCameCase}
   */
  public static WordCaseToCameCase wordCaseToCameCase() {
    return new WordCaseToCameCase();
  }

  @Override
  public String map(String name) {
    Check.that(name, "name").isNot(empty());
    return toLowerCase(name.charAt(0)) + name.substring(1);
  }
}
