package nl.naturalis.yokete.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.naturalis.common.util.MutableInt;
import static java.util.stream.Collectors.toUnmodifiableList;
import static nl.naturalis.common.CollectionMethods.asIntArray;

class SQLFactory {

  private static final String ERR_ADJACENT_PARAMS =
      "Adjacent parameters cannot yield valid SQL (positions %d,%d)";
  private static final String ERR_EMPTY_NAME = "Zero-length parameter name at position %d";

  // The processed SQL string in which all named parameters have
  // been replaced with positional parameters
  private final String normalized;
  private final Map<String, int[]> paramMap;
  private final List<NamedParameter> params;

  @SuppressWarnings("unchecked")
  SQLFactory(String sql) {
    StringBuilder out = new StringBuilder(sql.length());
    Map<String, List<Integer>> paramMap = new LinkedHashMap<>();
    MutableInt pCount = new MutableInt();
    int pStartPos = -1;
    boolean inString = false;
    boolean escaped = false;
    StringBuilder param = null;
    for (int i = 0; i < sql.length(); ++i) {
      char c = sql.charAt(i);
      if (inString) {
        out.append(c);
        if (c == '\'') {
          if (!escaped) inString = false;
          else escaped = true;
        } else if (c == '\\') escaped = true;
        else escaped = false;
      } else if (pStartPos != -1) { // we are assembling a parameter name
        if (isParamChar(c)) {
          param.append(c);
          if (i == sql.length() - 1) {
            addParam(paramMap, param, pCount, pStartPos);
          }
        } else {
          addParam(paramMap, param, pCount, pStartPos);
          out.append(c);
          pStartPos = -1;
          if (c == '\'') {
            inString = true;
          } else if (c == ':') {
            throw new KSQLException(ERR_ADJACENT_PARAMS, pStartPos, i);
          }
        }
      } else if (c == ':') {
        out.append('?');
        pStartPos = i;
        param = new StringBuilder();
      } else {
        out.append(c);
        if (c == '\'') {
          inString = true;
        }
      }
    }
    this.normalized = out.toString();
    this.paramMap =
        Map.ofEntries(paramMap.entrySet().stream().map(this::toNewEntry).toArray(Map.Entry[]::new));
    this.params =
        paramMap.entrySet().stream().map(this::toNamedParam).collect(toUnmodifiableList());
  }

  private NamedParameter toNamedParam(Entry<String, List<Integer>> e) {
    return new NamedParameter(e.getKey(), asIntArray(e.getValue()));
  }

  String sql() {
    return normalized;
  }

  List<NamedParameter> params() {
    return params;
  }

  Map<String, int[]> paramMap() {
    return paramMap;
  }

  private static boolean isParamChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private static void addParam(
      Map<String, List<Integer>> paramMap,
      StringBuilder param,
      MutableInt paramCount,
      int startPos) {
    if (param.length() == 0) {
      throw new KSQLException(ERR_EMPTY_NAME, startPos);
    }
    paramMap.computeIfAbsent(param.toString(), k -> new ArrayList<>(4)).add(paramCount.ppi());
  }

  private Entry<String, int[]> toNewEntry(Entry<String, List<Integer>> e) {
    return Map.entry(e.getKey(), asIntArray(e.getValue()));
  }
}
