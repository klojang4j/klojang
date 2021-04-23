package nl.naturalis.yokete.util;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static java.sql.Types.DATE;
import static nl.naturalis.yokete.util.ColumnReaders.GET_DATE;

class ToLocalDateSynapses {

  private ToLocalDateSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {
    Map<Integer, Synapse<?, LocalDate>> tmp = new HashMap<>();
    tmp.put(DATE, new Synapse<>(GET_DATE, Date::toLocalDate));
    return Map.copyOf(tmp);
  }
}
