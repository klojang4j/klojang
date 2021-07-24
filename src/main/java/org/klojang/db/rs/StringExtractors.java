package org.klojang.db.rs;

import java.util.HashMap;
import static org.klojang.db.rs.ExtractorNegotiator.DEFAULT;
import static org.klojang.db.rs.RsMethod.GET_STRING;

class StringExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  StringExtractors() {
    put(DEFAULT, new RsExtractor<>(GET_STRING));
  }
}
