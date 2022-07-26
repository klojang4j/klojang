package org.klojang.util.namemappers;

import org.klojang.template.NameMapper;
import nl.naturalis.check.Check;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static nl.naturalis.common.StringMethods.trim;
import static nl.naturalis.check.CommonChecks.empty;

/**
 * Converts snake case identifiers to word case identifiers. For example {@code my_bloody_valentine}
 * becomes {@code MyBloodyValentine}. Note that it doesn't matter whether you use lowercase letters
 * or uppercase letters or both in your snake case identifiers. {@code MY_BLOODY_VALENTINE} and
 * {@code My_Bloody_Valentine} would also become {@code MyBloodyValentine}.
 *
 * @author Ayco Holleman
 */
public class SnakeCaseToWordCase implements NameMapper {

  /**
   * Returns an instance of {@code SnakeCaseToWordCase}.
   *
   * @return An instance of {@code SnakeCaseToWordCase}
   */
  public static SnakeCaseToWordCase snakeCaseToWordCase() {
    return new SnakeCaseToWordCase();
  }

  @Override
  public String map(String name) {
    Check.notNull(name, "name");
    String in = trim(name, " _\t\r\n");
    Check.that(in).isNot(empty(), "Cannot map \"%s\"", name);
    char[] out = new char[in.length()];
    out[0] = toUpperCase(in.charAt(0));
    boolean nextWord = false;
    int j = 1;
    for (int i = 1; i < in.length(); ++i) {
      char c = in.charAt(i);
      if (c == '_') {
        nextWord = true;
      } else {
        out[j++] = nextWord ? toUpperCase(c) : toLowerCase(c);
        nextWord = false;
      }
    }
    return new String(out, 0, j);
  }
}
