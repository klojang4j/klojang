package org.klojang.helpers;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.klojang.db.Row;
import org.klojang.render.RenderException;
import nl.naturalis.common.Bool;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static org.klojang.x.StandardStringifiers.ESCAPE_ATTR;
import static org.klojang.x.StandardStringifiers.ESCAPE_HTML;
import static nl.naturalis.common.ObjectMethods.isEmpty;
import static nl.naturalis.common.ObjectMethods.isNotEmpty;
import static nl.naturalis.common.StringMethods.EMPTY;
import static nl.naturalis.common.StringMethods.append;
import static nl.naturalis.common.check.CommonChecks.gte;
import static nl.naturalis.common.check.CommonChecks.sameAs;

/**
 * A helper class that assists in generating the {@code <option>} elements for an HTML list box.
 * Note that this class does not generate the {@code <select>} element itself.
 *
 * @author Ayco Holleman
 */
public class ListBoxOptions {

  private static final String COL_OPTGROUP = "optgroup";
  private static final String COL_SELECTED = "optionSelected";
  private static final String COL_TEXT = "optionText";
  private static final String COL_VALUE = "optionValue";

  private static final String FOO = new String("__FOO__");
  private static final String BAR = new String("__BAR__");

  private Object initOptVal = EMPTY;
  private Object initOptText;
  private Supplier<List<Row>> options;
  private Integer selectedIndex;
  private Object selectedValue = BAR;

  /** Creates a new {@code ListBoxOptions} instance. */
  public ListBoxOptions() {}

  /**
   * Sets the text contents of the first option in the list box. For example an option like "{@code
   * -- please select --}". The value of the option is set to the empty string.
   *
   * @param text The text contents of the option.
   * @return This {@code ListBoxOptions} instance
   */
  public ListBoxOptions withInitOption(Object text) {
    return withInitOption(EMPTY, text);
  }

  /**
   * Sets the value and text contents of the first option in the list box.
   *
   * @param value The value of the {@code value} attribute of the option
   * @param text The text contents of the option.
   * @return This {@code ListBoxOptions} instance
   */
  public ListBoxOptions withInitOption(Object value, Object text) {
    Check.notNull(value, "value");
    Check.notNull(text, "text");
    this.initOptVal = value;
    this.initOptText = text;
    return this;
  }

  /**
   * Sets the {@code Supplier} of the data for the {@code <option>} elements. This must be a {@code
   * Supplier} of a {@code List} of {@link Row} instances. For each {@code Row} the following
   * applies:
   *
   * <p>
   *
   * <ol>
   *   <li>It <i>must</i> contain a key named <b>optionText</b>. Its value will be used as the text
   *       contents for the {@code <option>} element.
   *   <li>It <i>may</i> contain a key named <b>optionValue</b>. If present, its value will be used
   *       as the value of the {@code value} attribute of the {@code <option>} element.
   *   <li>It <i>may</i> contain a key named <b>optionSelected</b>. Its value must be "selected" or
   *       any value that evaluates to {@code true} or {@code false} when passing it to {@link
   *       Bool#from(Object)}. If present (and {@code true}), the option will be selected.
   *   <li>It <i>may</i> contain a key named <b>optgroup</b>. If present, its value will be used to
   *       group the {@code <option>} elements. If some {@code Row} instances do and others don't
   *       contain this key, the ones that don't will become the first options, placed outside any
   *       {@code <optgroup>}.
   *   <li>It <i>may</i> contain any number of keys whose name start with "data-". These will be
   *       copied as-is as data attributes for the {@code <option>} element.
   *   <li>It <i>may</i> contain any number of other keys, but these will be ignored.
   * </ol>
   *
   * @param options A @code Supplier} of the data for the {@code <option>} elements
   * @return This {@code ListBoxOptions} instance
   */
  public ListBoxOptions withOptionData(Supplier<List<Row>> options) {
    this.options = Check.notNull(options).ok();
    return this;
  }

  /**
   * Sets the data for the {@code <option>} elements. See {@link #withOptionData(List)}.
   *
   * @param options The data for the {@code <option>} elements
   * @return This {@code ListBoxOptions} instance
   */
  public ListBoxOptions withOptionData(List<Row> options) {
    Check.notNull(options);
    this.options = () -> options;
    return this;
  }

  /**
   * Makes the option with the specified array index the selected option.
   *
   * @param value The index of the option to be selected
   * @return This {@code ListBoxOptions} instance
   */
  public ListBoxOptions selectOption(int optionIndex) {
    selectedIndex = Check.that(optionIndex).is(gte(), 0).intValue();
    selectedValue = BAR;
    return this;
  }

  /**
   * Makes the option with the selected value the selected option.
   *
   * @param value The value of the option to be selected
   * @return This {@code ListBoxOptions} instance
   */
  public ListBoxOptions selectValue(Object value) {
    selectedValue = value;
    selectedIndex = null;
    return this;
  }

  /**
   * Generates the HTML for the {@code <option>} elements.
   *
   * @return The HTML for the {@code <option>} elements
   */
  public String getHTML() {
    StringBuilder sb = new StringBuilder(512);
    try {
      processInitOption(sb);
      List<Row> rows;
      if (options != null && isNotEmpty(rows = options.get())) {
        applySelection(rows);
        Map<String, List<Row>> optgroups = new LinkedHashMap<>();
        for (Row row : rows) {
          String s = row.hasColumn(COL_OPTGROUP) ? row.getString(COL_OPTGROUP) : null;
          optgroups.computeIfAbsent(s, k -> new ArrayList<>()).add(row);
        }
        processRows(sb, optgroups.get(null));
        for (Entry<String, List<Row>> e : optgroups.entrySet()) {
          if (e.getKey() != null) {
            append(sb, "<optgroup label=\"", ESCAPE_ATTR.toString(e.getKey()), "\">");
            processRows(sb, e.getValue());
            append(sb, "</optgroup>");
          }
        }
      }
      sb.append("</select>");
    } catch (RenderException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return sb.toString();
  }

  private void processInitOption(StringBuilder sb) throws RenderException {
    if (initOptText != null) {
      append(sb, "<option value=\"", ESCAPE_ATTR.toString(initOptVal), '"');
      append(sb, '>', ESCAPE_HTML.toString(initOptText), "</option>");
    }
  }

  private void applySelection(List<Row> rows) {
    if (selectedIndex != null) {
      int idx = selectedIndex;
      if (idx < rows.size()) {
        for (int i = 0; i < rows.size(); ++i) {
          rows.get(i).setOrAddColumn(COL_SELECTED, idx == i);
        }
      }
    } else if (selectedValue != BAR) {
      for (Row r : rows) {
        if (r.hasColumn(COL_VALUE)) {
          r.setOrAddColumn(COL_SELECTED, Objects.equals(selectedValue, r.getValue(COL_VALUE)));
        } else if (r.hasColumn(COL_TEXT)) {
          r.setOrAddColumn(COL_SELECTED, Objects.equals(selectedValue, r.getValue(COL_TEXT)));
        }
      }
    }
  }

  private static void processRows(StringBuilder sb, List<Row> rows) throws RenderException {
    if (isEmpty(rows)) {
      return;
    }
    for (Row row : rows) {
      String content = FOO;
      sb.append("<option");
      for (String colName : row.getColumnNames()) {
        String v = row.getString(colName);
        if (colName.equals(COL_TEXT)) {
          content = v;
        } else if (COL_VALUE.equals(colName)) {
          append(sb, " value=\"", ESCAPE_ATTR.toString(v), "\"");
        } else if (COL_SELECTED.equals(colName)) {
          if ("selected".equalsIgnoreCase(v) || Bool.from(v)) {
            sb.append(" selected=\"selected\"");
          }
        } else if (colName.startsWith("data-")) {
          append(sb, ' ', colName, "=\"", ESCAPE_ATTR.toString(v), "\"");
        }
      }
      Check.that(content).isNot(sameAs(), FOO, "Missing \"content\" column");
      append(sb, '>', ESCAPE_HTML.toString(content), "</option>");
    }
  }

  /** Returns the result of {@link #getHTML()}. */
  public String toString() {
    return getHTML();
  }
}
