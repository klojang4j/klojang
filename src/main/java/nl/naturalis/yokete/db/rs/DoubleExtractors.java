package nl.naturalis.yokete.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.RsMethod.*;

class DoubleExtractors extends ExtractorLookup<Double> {

  DoubleExtractors() {
    addMore(new RsExtractor<>(GET_DOUBLE), FLOAT, DOUBLE);
    // We don't really need to add the conversion functions (like  Integer::doubleValue)
    // because the compiler can figure this out by itself. But we like to be explicit.
    add(INTEGER, new RsExtractor<>(GET_INT, Integer::doubleValue));
    add(SMALLINT, new RsExtractor<>(GET_SHORT, Short::doubleValue));
    add(TINYINT, new RsExtractor<>(GET_BYTE, Byte::doubleValue));
    add(REAL, new RsExtractor<>(GET_FLOAT, Float::doubleValue));
    add(BIGINT, new RsExtractor<>(GET_LONG, Long::doubleValue));
    add(BOOLEAN, new RsExtractor<>(GET_BOOLEAN, x -> x ? 1.0 : 0));
    addMore(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    add(VARCHAR, new RsExtractor<>(GET_STRING, NumberMethods::parse));
  }
}
