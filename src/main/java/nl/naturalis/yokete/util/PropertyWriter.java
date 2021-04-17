package nl.naturalis.yokete.util;

import nl.naturalis.common.invoke.Setter;

class PropertyWriter extends ColumnReader {

  private final Setter<?> setter;

  PropertyWriter(int idx, String label, int type, ResultSetGetter getter, Setter<?> setter) {
    super(idx, label, type, getter);
    this.setter = setter;
  }
}
