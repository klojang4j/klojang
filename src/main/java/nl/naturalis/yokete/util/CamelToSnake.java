package nl.naturalis.yokete.util;

import java.util.function.UnaryOperator;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

/**
 * Converts camel case identifiers to snake case identifiers. For example "myBloodyValentine"
 * becomes "my_bloody_valentine".
 *
 * @author Ayco Holleman
 */
public class CamelToSnake implements UnaryOperator<String> {

  private static final CamelToSnake INSTANCE = new CamelToSnake();

  public static final CamelToSnake camelToSnake() {
    return INSTANCE;
  }

  @Override
  public String apply(String n) {
    int maxLen = (int) Math.ceil(n.length() * 1.5F);
    char[] colName = new char[maxLen];
    colName[0] = isUpperCase(n.charAt(0)) ? toLowerCase(n.charAt(0)) : n.charAt(0);
    int j = 1;
    for (int i = 1; i < n.length(); ++i) {
      if (isUpperCase(n.charAt(i))) {
        if ((i != (n.length() - 1)) && isLowerCase(n.charAt(i + 1))) {
          colName[j++] = '_';
          colName[j++] = toLowerCase(n.charAt(i));
        } else if (isLowerCase(n.charAt(i - 1))) {
          colName[j++] = '_';
          colName[j++] = toLowerCase(n.charAt(i));
        } else {
          colName[j++] = toLowerCase(n.charAt(i));
        }
      } else {
        colName[j++] = n.charAt(i);
      }
    }
    return new String(colName, 0, j);
  }
}
