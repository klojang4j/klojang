package org.klojang.x.db.rs;

import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static org.klojang.x.db.rs.RsMethod.*;

class ByteExtractors extends ExtractorLookup<Byte> {

  ByteExtractors() {
    add(TINYINT, new RsExtractor<Byte, Byte>(GET_BYTE));
    add(INTEGER, new RsExtractor<Integer, Byte>(GET_INT, NumberMethods::convert));
    add(SMALLINT, new RsExtractor<Short, Byte>(GET_SHORT, NumberMethods::convert));
    add(REAL, new RsExtractor<Float, Byte>(GET_FLOAT, NumberMethods::convert));
    add(BIGINT, new RsExtractor<Long, Byte>(GET_LONG, NumberMethods::convert));
    addMultiple(new RsExtractor<>(GET_DOUBLE, NumberMethods::convert), FLOAT, DOUBLE);
    addMultiple(new RsExtractor<>(GET_BIG_DECIMAL, NumberMethods::convert), NUMERIC, DECIMAL);
    addMultiple(new RsExtractor<>(GET_STRING, NumberMethods::parse), VARCHAR, CHAR);
  }
}
