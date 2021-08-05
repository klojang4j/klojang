package org.klojang.util.namemappers;

import org.klojang.render.NameMapper;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.StringMethods.trim;
import static nl.naturalis.common.check.CommonChecks.empty;
import static java.lang.Character.*;

public class SnakeCaseToCamelCase implements NameMapper {

  public SnakeCaseToCamelCase() {}

  @Override
  public String map(String name) {
    String in = trim(Check.notNull(name, "name").ok().strip(), "_");
    Check.that(in).isNot(empty(), "Invalid name: \"%s\"", name);
    char[] out = new char[in.length()];
    out[0] = toLowerCase(in.charAt(0));
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
