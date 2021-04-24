package nl.naturalis.yokete.db;

import java.util.Map;
import static nl.naturalis.yokete.db.SynapseNegotiator.DEFAULT_ENTRY;

class ToStringSynapses {

  private ToStringSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {
    // If the Java type is String.class we always call ResultSet.getString()
    // no matter the actual SQL type of the column we are reading.
    // ResultSet.getString() always works (as far as we know)
    Synapse<String, String> syn = new Synapse<>(ColumnReader.GET_STRING);
    return Map.of(DEFAULT_ENTRY, syn);
  }
}
