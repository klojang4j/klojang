package nl.naturalis.yokete.helpers;

import java.util.List;
import java.util.function.Supplier;
import nl.naturalis.common.Bool;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.Row;
import nl.naturalis.yokete.render.RenderException;
import static nl.naturalis.common.StringMethods.EMPTY;
import static nl.naturalis.common.StringMethods.append;
import static nl.naturalis.common.check.CommonChecks.yes;
import static nl.naturalis.yokete.x.render.StandardStringifiers.ESCAPE_ATTR;
import static nl.naturalis.yokete.x.render.StandardStringifiers.ESCAPE_HTML;

public class ListBoxOptions {

  private static final String ERR0 =
      "Row must contain a column named \"text\" containing "
          + "the text content of the <option> element";

  private Object initOptVal = EMPTY;
  private Object initOptText;
  private String initOptAttrs;
  private Supplier<List<Row>> options;

  public ListBoxOptions() {}

  public ListBoxOptions withInitOption(Object text) {
    return withInitOption(EMPTY, text);
  }

  public ListBoxOptions withInitOption(Object value, Object text) {
    return withInitOption(value, text, null);
  }

  public ListBoxOptions withInitOption(Object value, Object text, String attributes) {
    Check.notNull(value, "value");
    Check.notNull(text, "text");
    this.initOptVal = value;
    this.initOptText = text;
    this.initOptAttrs = attributes;
    return this;
  }

  public ListBoxOptions withOptionData(Supplier<List<Row>> options) {
    this.options = options;
    return this;
  }

  public ListBoxOptions withOptionData(List<Row> options) {
    this.options = () -> options;
    return this;
  }

  public String getHTML() throws RenderException {
    StringBuilder sb = new StringBuilder(512);
    if (initOptText != null) {
      append(sb, "<option value=\"", ESCAPE_ATTR.toString(initOptVal), '"');
      if (initOptAttrs != null) {
        append(sb, ' ', initOptAttrs);
      }
      append(sb, '>', ESCAPE_HTML.toString(initOptText), "</option>");
    }
    List<Row> rows;
    if (options != null && (rows = options.get()) != null) {
      for (Row row : rows) {
        Check.that(row.hasColumn("text")).is(yes(), ERR0);
        sb.append("<option");
        for (String colName : row.getColumnNames()) {
          if (colName.equals("text")) {
            continue;
          }
          String value = row.getString(colName);
          if ("selected".equalsIgnoreCase(colName)) {
            if ("selected".equalsIgnoreCase(value) || Bool.from(value)) {
              sb.append(" selected=\"selected\"");
            }
          } else {
            append(sb, ' ', colName, "=\"", ESCAPE_ATTR.toString(value), "\"");
          }
        }
        append(sb, '>', ESCAPE_HTML.toString(row.getValue("text")), "</option>");
      }
    }
    sb.append("</select>");
    return sb.toString();
  }

  public String toString() {
    try {
      return getHTML();
    } catch (RenderException e) {
      return e.toString();
    }
  }
}
