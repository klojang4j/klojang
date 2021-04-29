package nl.naturalis.yokete.db.write;

import java.util.function.Function;

public class ValueAbsorber<FIELD_TYPE, COLUMN_TYPE> {

  private final ColumnWriter<COLUMN_TYPE> writer;
  private final Adapter<FIELD_TYPE, COLUMN_TYPE> adapter;

  ValueAbsorber(ColumnWriter<COLUMN_TYPE> writer) {
    this.writer = writer;
    this.adapter = null;
  }

  ValueAbsorber(ColumnWriter<COLUMN_TYPE> writer, Function<FIELD_TYPE, COLUMN_TYPE> adapter) {
    this(writer, (x, y) -> adapter.apply(x));
  }

  ValueAbsorber(ColumnWriter<COLUMN_TYPE> writer, Adapter<FIELD_TYPE, COLUMN_TYPE> adapter) {
    this.writer = writer;
    this.adapter = adapter;
  }
}
