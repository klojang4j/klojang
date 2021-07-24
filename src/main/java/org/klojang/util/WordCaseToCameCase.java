package org.klojang.util;

import java.util.function.UnaryOperator;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.empty;

public class WordCaseToCameCase implements UnaryOperator<String> {

  @Override
  public String apply(String s) {
    char[] chars = Check.that(s).isNot(empty()).ok().toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return String.valueOf(chars);
  }
}
