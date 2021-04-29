package nl.naturalis.yokete.db.read;

import java.util.HashMap;
import static nl.naturalis.yokete.db.read.ColumnReader.GET_STRING;
import static nl.naturalis.yokete.db.read.ValueProducerNegotiator.DEFAULT_ENTRY;

class StringProducers extends HashMap<Integer, ValueProducer<?, ?>> {

  StringProducers() {
    put(DEFAULT_ENTRY, new ValueProducer<>(GET_STRING));
  }
}
