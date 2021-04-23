package nl.naturalis.yokete.util;

import java.time.format.DateTimeFormatter;

public abstract class ResultSetReaderConfig {

  public DateTimeFormatter getLocaDateFormatter() {
    return null;
  }

  public DateTimeFormatter getLocaDateTimeFormatter() {
    return null;
  }
}
