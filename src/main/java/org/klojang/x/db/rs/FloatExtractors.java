package org.klojang.x.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;

public class FloatExtractors extends ExtractorLookup<Float> {

  public FloatExtractors() {
    add(FLOAT, new RsExtractor<>(GET_FLOAT));
    add(INTEGER, new RsExtractor<>(GET_INT, Integer::floatValue));
    add(SMALLINT, new RsExtractor<>(GET_SHORT, Short::floatValue));
    add(TINYINT, new RsExtractor<>(GET_BYTE, Byte::floatValue));
    add(REAL, new RsExtractor<>(GET_FLOAT, Float::floatValue));
    add(BIGINT, new RsExtractor<>(GET_LONG, Long::floatValue));
    add(BOOLEAN, new RsExtractor<>(GET_BOOLEAN, x -> x ? 1.0F : 0));
    addMultiple(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    add(VARCHAR, new RsExtractor<>(GET_STRING, NumberMethods::parse));
  }
}
