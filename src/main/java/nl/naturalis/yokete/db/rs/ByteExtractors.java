package nl.naturalis.yokete.db.rs;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.NumberMethods;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.rs.ColumnReader.*;
import static nl.naturalis.yokete.db.rs.ExtractorNegotiator.DEFAULT_ENTRY;

class ByteExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  ByteExtractors() {

    put(TINYINT, new ValueExtractor<Byte, Byte>(GET_BYTE));
    put(INTEGER, new ValueExtractor<Integer, Byte>(GET_INT, NumberMethods::convert));
    put(SMALLINT, new ValueExtractor<Short, Byte>(GET_SHORT, NumberMethods::convert));
    put(REAL, new ValueExtractor<Float, Byte>(GET_FLOAT, NumberMethods::convert));
    put(BIGINT, new ValueExtractor<Long, Byte>(GET_LONG, NumberMethods::convert));
    ValueExtractor<Double, Byte> vp0 = new ValueExtractor<>(GET_DOUBLE, NumberMethods::convert);
    put(FLOAT, vp0);
    put(DOUBLE, vp0);
    ColumnReader<BigDecimal> cr = GET_BIG_DECIMAL;
    ValueExtractor<BigDecimal, Byte> vp1 = new ValueExtractor<>(cr, NumberMethods::convert);
    put(NUMERIC, vp1);
    put(DECIMAL, vp1);
    ValueExtractor<String, Byte> vp2 = new ValueExtractor<>(GET_STRING, NumberMethods::parse);
    put(VARCHAR, vp2);
    put(DEFAULT_ENTRY, vp2);
  }
}
