package org.klojang.util.namemappers;

import org.klojang.render.NameMapper;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

/**
 * Converts camel case identifiers to snake case identifiers. For example {@code myBloodyValentine}
 * becomes {@code my_bloody_valentine}.
 *
 * @author Ayco Holleman
 */
public class CamelCaseToSnakeLowerCase implements NameMapper {

  @Override
  public String map(String s) {
    int maxLen = (int) Math.ceil(s.length() * 1.5F);
    char[] out = new char[maxLen];
    out[0] = isUpperCase(s.charAt(0)) ? toLowerCase(s.charAt(0)) : s.charAt(0);
    int j = 1;
    for (int i = 1; i < s.length(); ++i) {
      if (isUpperCase(s.charAt(i))) {
        if ((i != (s.length() - 1)) && isLowerCase(s.charAt(i + 1))) {
          out[j++] = '_';
          out[j++] = toLowerCase(s.charAt(i));
        } else if (isLowerCase(s.charAt(i - 1))) {
          out[j++] = '_';
          out[j++] = toLowerCase(s.charAt(i));
        } else {
          out[j++] = toLowerCase(s.charAt(i));
        }
      } else {
        out[j++] = s.charAt(i);
      }
    }
    return new String(out, 0, j);
  }
}