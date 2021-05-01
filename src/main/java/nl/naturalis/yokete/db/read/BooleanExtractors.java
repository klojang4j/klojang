package nl.naturalis.yokete.db.read;

import java.math.BigDecimal;
import java.util.HashMap;
import nl.naturalis.common.Bool;
import static java.sql.Types.*;
import static nl.naturalis.yokete.db.read.ColumnReader.*;
import static nl.naturalis.yokete.db.read.ExtractorNegotiator.DEFAULT_ENTRY;

class BooleanExtractors extends HashMap<Integer, ValueExtractor<?, ?>> {

  BooleanExtractors() {
    ValueExtractor<Boolean, Boolean> vp0 = new ValueExtractor<>(GET_BOOLEAN);
    put(BOOLEAN, vp0);
    put(BIT, vp0);
    put(INTEGER, new ValueExtractor<Integer, Boolean>(GET_INT, Bool::from));
    put(SMALLINT, new ValueExtractor<Short, Boolean>(GET_SHORT, Bool::from));
    put(TINYINT, new ValueExtractor<Byte, Boolean>(GET_BYTE, Bool::from));
    ValueExtractor<Double, Boolean> vp1 = new ValueExtractor<>(GET_DOUBLE, Bool::from);
    put(FLOAT, vp1);
    put(DOUBLE, vp1);
    put(BIGINT, new ValueExtractor<Long, Boolean>(GET_LONG, Bool::from));
    put(REAL, new ValueExtractor<Float, Boolean>(GET_FLOAT, Bool::from));
    ValueExtractor<BigDecimal, Boolean> vp2 = new ValueExtractor<>(GET_BIG_DECIMAL, Bool::from);
    put(NUMERIC, vp2);
    put(DECIMAL, vp2);
    ValueExtractor<String, Boolean> vp3 = new ValueExtractor<>(GET_STRING, Bool::from);
    put(VARCHAR, vp3);
    put(DEFAULT_ENTRY, vp3);
  }
}
