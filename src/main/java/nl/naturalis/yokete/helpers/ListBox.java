package nl.naturalis.yokete.helpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import nl.naturalis.common.Bool;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.db.Row;
import static nl.naturalis.common.ObjectMethods.n2e;
import static nl.naturalis.common.StringMethods.EMPTY;
import static nl.naturalis.common.StringMethods.append;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.check.CommonChecks.yes;
import static nl.naturalis.common.check.CommonGetters.key;
import static nl.naturalis.common.check.CommonGetters.value;
import static nl.naturalis.yokete.render.EscapeType.ESCAPE_ATTR;
import static nl.naturalis.yokete.render.EscapeType.ESCAPE_HTML;

public class ListBox {

  private static final String ERR0 =
      "Row must contain a column named \"text\" containing "
          + "the text content of the <option> element";
  private static final String ERR1 = "Attribute name must not be null";
  private static final String ERR2 = "Attribute value must not be null";

  private String id;
  private String name;
  private Map<String, String> attrs;
  private Tuple<Object, Object> initOption;
  private Supplier<List<Row>> options;

  public ListBox() {}

  public ListBox withId(String id) {
    this.id = id;
    return this;
  }

  public ListBox withName(String name) {
    this.name = name;
    return this;
  }

  public ListBox withAttribute(String name, String value) {
    Check.that(name).is(notNull(), ERR1);
    Check.that(value).is(notNull(), ERR2);
    if (attrs == null) {
      attrs = new LinkedHashMap<>(4);
    }
    attrs.put(name, value);
    return this;
  }

  public ListBox withAttributes(Map<String, String> attrs) {
    for (Map.Entry<String, String> entry : attrs.entrySet()) {
      Check.that(entry).has(key(), notNull(), ERR1).has(value(), notNull(), ERR2);
    }
    this.attrs = new LinkedHashMap<>(attrs);
    return this;
  }

  public ListBox withInitOption(Object text) {
    return withInitOption(EMPTY, text);
  }

  public ListBox withInitOption(Object value, Object text) {
    Check.notNull(value, "value");
    Check.notNull(text, "text");
    this.initOption = Tuple.of(value, text);
    return this;
  }

  public ListBox withOptions(Supplier<List<Row>> options) {
    this.options = options;
    return this;
  }

  public ListBox withOptions(List<Row> options) {
    this.options = () -> options;
    return this;
  }

  public String getHTML() {
    StringBuilder sb = new StringBuilder(512);
    sb.append("<select");
    if (id != null) {
      append(sb, " id=\"", ESCAPE_ATTR.apply(id), '"');
    }
    if (name != null) {
      append(sb, " name=\"", ESCAPE_ATTR.apply(name), '"');
    }
    if (attrs != null) {
      attrs.forEach((k, v) -> append(sb, ' ', k, "=\"", ESCAPE_ATTR.apply(v), '"'));
    }
    sb.append(">");
    if (initOption != null) {
      append(
          sb,
          "<option value=\"",
          ESCAPE_ATTR.apply(n2e(initOption.getLeft())),
          "\">",
          ESCAPE_HTML.apply(n2e(initOption.getRight())),
          "</option>");
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
          String value = n2e(row.getValue(colName));
          if ("selected".equalsIgnoreCase(colName)) {
            if ("selected".equalsIgnoreCase(value) || Bool.from(value)) {
              sb.append(" selected=\"selected\"");
            }
          } else {
            append(sb, ' ', colName, "=\"", ESCAPE_ATTR.apply(value), "\"");
          }
        }
        append(sb, '>', ESCAPE_HTML.apply(n2e(row.getString("text"))), "</option>");
      }
    }
    sb.append("</select>");
    return sb.toString();
  }

  public String toString() {
    return getHTML();
  }
}
