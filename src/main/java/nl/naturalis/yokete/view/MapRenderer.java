package nl.naturalis.yokete.view;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;
import org.apache.commons.text.StringEscapeUtils;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_HTML;
import static nl.naturalis.yokete.view.EscapeType.ESCAPE_JS;
import static nl.naturalis.yokete.view.EscapeType.*;

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

  private List<Part> parts;
  private List<String> out;

  public void start() {
    Check.with(illegalState(), parts).is(nullPointer(), ERR_NOT_RESET);
    lock.lock();
    parts = template.getParts();
    out = new ArrayList<>(Arrays.asList(new String[parts.size()]));
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
    processVars(myData, escapeType);
  }

  public StringBuilder render() {
    Check.with(illegalState(), lock.isLocked()).is(yes(), ERR_NOT_STARTED);
    int sz = out.stream().mapToInt(String::length).sum();
    StringBuilder sb = new StringBuilder(sz);
    out.forEach(sb::append);
    return sb;
  }

  public void render(StringBuilder sb) {
    Check.with(illegalState(), lock.isLocked()).is(yes(), ERR_NOT_STARTED);
    parts.forEach(sb::append);
  }

  public void reset() {
    parts = null;
    out = null;
    lock.unlock();
  }

  // All parts containing the name of a variable are overwritten with
  // the value of that variable
  private void processVars(Map<String, Object> data, EscapeType escapeType) {
    template.getVarIndices().forEach(index -> processVar(index, data, escapeType));
  }

  private void processVar(int index, Map<String, Object> data, EscapeType escapeType) {
    VariablePart vp = VariablePart.class.cast(parts.get(index));
    String var = vp.getName();
    EscapeType et = vp.getEscapeType();
    if (data.containsKey(var)) {
      Object val = data.get(var);
      if (val == null) {
        out.set(index, whenNull.apply(var));
      } else if (escapingNotNecessary(val)) {
        out.set(index, val.toString());
      } else if (et == NOT_SPECIFIED || et == escapeType) {
        if (escapeType == ESCAPE_NONE || escapingNotNecessary(val)) {
          out.set(index, val.toString());
        } else if (escapeType == ESCAPE_HTML) {
          out.set(index, StringEscapeUtils.escapeHtml4(val.toString()));
        } else if (escapeType == ESCAPE_JS) {
          out.set(index, StringEscapeUtils.escapeEcmaScript(val.toString()));
        }
      }
    }
  }

  private static boolean escapingNotNecessary(Object val) {
    return val instanceof Number || val instanceof Boolean;
  }
}
