package nl.naturalis.yokete.db.rs;

import java.util.HashMap;
import static nl.naturalis.yokete.db.rs.RsMethod.GET_STRING;
import static nl.naturalis.yokete.db.rs.ExtractorNegotiator.DEFAULT;

class StringExtractors extends HashMap<Integer, RsExtractor<?, ?>> {

  StringExtractors() {
    put(DEFAULT, new RsExtractor<>(GET_STRING));
  }
}
