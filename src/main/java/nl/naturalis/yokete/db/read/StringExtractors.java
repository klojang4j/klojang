package nl.naturalis.yokete.db.read;

import java.util.HashMap;
import static nl.naturalis.yokete.db.read.ColumnReader.GET_STRING;
import static nl.naturalis.yokete.db.read.ExtractorNegotiator.DEFAULT_ENTRY;

class StringExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  StringExtractors() {
    put(DEFAULT_ENTRY, new ValueExtractor<>(GET_STRING));
  }
}
