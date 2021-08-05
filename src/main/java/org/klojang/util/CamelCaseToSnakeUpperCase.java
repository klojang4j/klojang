package org.klojang.util;

import org.klojang.render.NameMapper;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;

/**
 * Converts camel case identifiers to snake case identifiers. For example {@code myBloodyValentine}
 * becomes {@code MY_BLOODY_VALENTINE}.
 *
 * @author Ayco Holleman
 */
public class CamelCaseToSnakeUpperCase implements NameMapper {

  @Override
  public String map(String n) {
    int maxLen = (int) Math.ceil(n.length() * 1.5F);
    char[] colName = new char[maxLen];
    colName[0] = toUpperCase(n.charAt(0));
    int j = 1;
    for (int i = 1; i < n.length(); ++i) {
      if (isUpperCase(n.charAt(i))) {
        if ((i != (n.length() - 1)) && isLowerCase(n.charAt(i + 1))) {
          colName[j++] = '_';
          colName[j++] = n.charAt(i);
        } else if (isLowerCase(n.charAt(i - 1))) {
          colName[j++] = '_';
          colName[j++] = toUpperCase(n.charAt(i));
        } else {
          colName[j++] = toUpperCase(n.charAt(i));
        }
      } else {
        colName[j++] = toUpperCase(n.charAt(i));
      }
    }
    return new String(colName, 0, j);
  }
}
