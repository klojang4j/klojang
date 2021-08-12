package org.klojang.x.db.rs;

import static java.sql.Types.CHAR;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static org.klojang.x.db.rs.RsMethod.GET_INT;
import static org.klojang.x.db.rs.RsMethod.GET_STRING;
import static nl.naturalis.common.StringMethods.EMPTY;

class StringExtractors extends ExtractorLookup<String> {

  private static final Adapter<Object, String> TO_STRING =
      (x, y) -> x == null ? EMPTY : x.toString();

  StringExtractors() {
    put(VARCHAR, new RsExtractor<>(GET_STRING));
    put(CHAR, new RsExtractor<>(GET_STRING));
    put(INTEGER, new RsExtractor<>(GET_INT, TO_STRING));
  }
}
