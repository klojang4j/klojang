package org.klojang.x.db.rs;

import java.util.HashMap;
import static org.klojang.x.db.rs.ExtractorNegotiator.DEFAULT;
import static org.klojang.x.db.rs.RsMethod.GET_STRING;

class StringExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  StringExtractors() {
    put(DEFAULT, new RsExtractor<>(GET_STRING));
  }
}
