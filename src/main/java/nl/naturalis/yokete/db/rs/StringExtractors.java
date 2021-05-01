package nl.naturalis.yokete.db.rs;

import java.util.HashMap;
import static nl.naturalis.yokete.db.rs.ColumnReader.GET_STRING;
import static nl.naturalis.yokete.db.rs.ExtractorNegotiator.DEFAULT_ENTRY;

class StringExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  StringExtractors() {
    put(DEFAULT_ENTRY, new ValueExtractor<>(GET_STRING));
  }
}
