package nl.naturalis.yokete.util;

class MapSynapse<COLUMN_TYPE> extends Synapse<COLUMN_TYPE, COLUMN_TYPE> {

  public MapSynapse(ColumnReader<COLUMN_TYPE> reader) {
    super(reader);
  }
}
