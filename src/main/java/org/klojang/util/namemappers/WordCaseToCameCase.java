package org.klojang.util.namemappers;

import org.klojang.render.NameMapper;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.empty;

/**
 * Converts word case identifiers to came cal identifiers. For exampl {@code MyBloodyValentine}
 * becomes {@code myBloodyValentine}.
 *
 * @author Ayco Holleman
 */
public class WordCaseToCameCase implements NameMapper {

  @Override
  public String map(String s) {
    char[] chars = Check.that(s).isNot(empty()).ok().toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return String.valueOf(chars);
  }
}
