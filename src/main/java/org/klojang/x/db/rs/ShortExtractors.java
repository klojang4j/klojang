package org.klojang.x.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;

class ShortExtractors extends ExtractorLookup<Short> {

  ShortExtractors() {
    add(SMALLINT, new RsExtractor<Short, Short>(GET_SHORT));
    add(TINYINT, new RsExtractor<Byte, Short>(GET_BYTE, NumberMethods::convert));
    add(INTEGER, new RsExtractor<Integer, Short>(GET_INT, NumberMethods::convert));
    add(REAL, new RsExtractor<Float, Short>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new RsExtractor<Long, Short>(GET_LONG, NumberMethods::convert));
    addMultiple(new RsExtractor<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMultiple(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    addMultiple(new RsExtractor<>(GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }
}
