package nl.naturalis.yokete.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;
import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.common.check.CommonChecks.nullPointer;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.common.check.CommonChecks.yes;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_HTML;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_JS;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_NONE;

/**
 * Renders template with data from a {@link Map}. The map is assumed to be flat (i.e. without nested
 * objects). Map values are rendered using {@link Object#toString()}.
 *
 * @author Ayco Holleman
 */
public class MapRenderer {

  private static final String ERR_NOT_STARTED = "Not started yet";
  private static final String ERR_NOT_RESET = "Awaiting reset";

  private final ReentrantLock lock = new ReentrantLock();

  private final Template template;

  private UnaryOperator<String> whenNull = x -> "";

  /**
   * Creates a {@code VariableSubstituter} that uses the {@link #REGEX_VARIABLE default variable
   * pattern} to extract variables from text.
   *
   * @param template
   */
  public MapRenderer(Template template) {
    this.template = template;
  }

  /**
   * Sets the function that generates a value for a variable whose value is null. By default an
   * empty string is inserted at the location of a variable whose value is null. For HTML files, for
   * example, you might want the variable to be substituted with a non-breaking space (&amp;nbsp;)
   * instead. The function is passed the name of the variable and should return an appropriate "null
   * value" for that variable.
   *
   * @param nullString
   */
  public MapRenderer whenNull(UnaryOperator<String> whenNull) {
    this.whenNull = whenNull;
    return this;
  }

  public StringBuilder render(Map<String, Object> data) {
    return render(data, ESCAPE_NONE);
  }

  public StringBuilder render(Map<String, Object> data, EscapeType escapeType) {
    try {
      start();
      substitute(data, escapeType, data.keySet());
      return render();
    } finally {
      reset();
    }
  }

  public void render(Map<String, Object> data, StringBuilder out) {
    render(data, out, ESCAPE_NONE);
  }

  public void render(Map<String, Object> data, StringBuilder out, EscapeType escapeType) {
    try {
      start();
      substitute(data, escapeType, data.keySet());
      render(out);
    } finally {
      reset();
    }
  }

  public void repeat(List<Map<String, Object>> data, StringBuilder out) {
    repeat(data, out, ESCAPE_NONE);
  }

  public StringBuilder repeat(List<Map<String, Object>> data) {
    return repeat(data, ESCAPE_NONE);
  }

  public void repeat(List<Map<String, Object>> data, StringBuilder out, EscapeType escapeType) {
    data.stream().forEach(map -> render(map, out, escapeType));
  }

  public StringBuilder repeat(List<Map<String, Object>> data, EscapeType escapeType) {
    StringBuilder out = new StringBuilder(512);
    data.stream().forEach(map -> render(map, out, escapeType));
    return out;
  }

  private List<String> parts;

  public void start() {
    Check.with(illegalState(), parts).is(nullPointer(), ERR_NOT_RESET);
    lock.lock();
    parts = template.getParts();
  }

  public void substitute(Map<String, Object> data, EscapeType escapeType, Set<String> fields) {
    Check.with(illegalState(), lock.isLocked()).is(yes(), ERR_NOT_STARTED);
    Map<String, Object> myData;
    if (fields == data.keySet()) {
      myData = data;
    } else {
      Check.that(fields, "fields").is(subsetOf(), data.keySet());
      myData = new HashMap<>(data);
      myData.keySet().retainAll(fields);
    }
    substitute(myData, escapeType);
  }

  public StringBuilder render() {
    Check.with(illegalState(), parts).is(notNull(), ERR_NOT_STARTED);
    int sz = parts.stream().mapToInt(String::length).sum();
    StringBuilder sb = new StringBuilder(sz);
    parts.forEach(sb::append);
    return sb;
  }

  public void render(StringBuilder sb) {
    Check.with(illegalState(), parts).is(notNull(), ERR_NOT_STARTED);
    parts.forEach(sb::append);
  }

  public void reset() {
    parts = null;
    lock.unlock();
  }

  // All parts containing the name of a variable are overwritten with
  // the value of that variable
  private void substitute(Map<String, Object> data, EscapeType escapeType) {
    template
        .getVarIndices()
        .forEach(
            index -> {
              String var = parts.get(index);
              if (data.containsKey(var)) {
                Object val = data.get(var);
                if (val == null) {
                  parts.set(index, whenNull.apply(var));
                } else if (escapeType == ESCAPE_NONE || escapingNotRequired(val)) {
                  parts.set(index, val.toString());
                } else if (escapeType == ESCAPE_HTML) {
                  parts.set(index, StringEscapeUtils.escapeHtml4(val.toString()));
                } else if (escapeType == ESCAPE_JS) {
                  parts.set(index, StringEscapeUtils.escapeEcmaScript(val.toString()));
                }
              }
            });
  }

  private static boolean escapingNotRequired(Object val) {
    return val instanceof Number || val instanceof Boolean;
  }
}
