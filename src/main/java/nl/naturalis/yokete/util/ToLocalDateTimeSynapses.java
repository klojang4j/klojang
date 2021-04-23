package nl.naturalis.yokete.util;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import static java.sql.Types.DATE;
import static nl.naturalis.yokete.util.ColumnReaders.GET_DATE;

class ToLocalDateTimeSynapses {

  private static final Function<Date, LocalDateTime> SQLDATE_TO_LOCALDATETIME =
      d -> d.toLocalDate().atStartOfDay();

  private ToLocalDateTimeSynapses() {}

  static Map<Integer, Synapse<?, ?>> get() {
    Map<Integer, Synapse<?, LocalDateTime>> tmp = new HashMap<>();
    tmp.put(DATE, new Synapse<>(GET_DATE, SQLDATE_TO_LOCALDATETIME));
    return Map.copyOf(tmp);
  }
}
