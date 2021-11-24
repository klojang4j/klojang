package org.klojang.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.collection.IntArrayList;
import nl.naturalis.common.collection.IntList;
import nl.naturalis.common.util.MutableInt;
import static nl.naturalis.common.CollectionMethods.convertAndFreeze;
import static nl.naturalis.common.CollectionMethods.freezeIntoList;
import static nl.naturalis.common.check.CommonChecks.blank;

/**
 * Extracts named parameters from a SQL query string and replaces them with positional parameters
 * (question marks).
 *
 * @author Ayco Holleman
 */
class SQLNormalizer {

  private static final String ERR_ADJACENT_PARAMS =
      "Adjacent parameters cannot yield valid SQL (at positions %d,%d)";
  private static final String ERR_EMPTY_NAME = "Zero-length parameter name at position %d";

  private final String unparsed;
  private final String normalized;
  private final Map<String, IntList> paramMap;
  private final List<NamedParameter> params;

  SQLNormalizer(String sql) {
    this.unparsed = Check.that(sql).isNot(blank(), "Empty SQL string").ok();
    Map<String, IntList> tmp = new LinkedHashMap<>();
    StringBuilder out = new StringBuilder(sql.length());
    MutableInt pCount = new MutableInt(); // parameter counter
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
            addParam(tmp, param, pCount, pStartPos);
          }
        } else {
          addParam(tmp, param, pCount, pStartPos);
          out.append(c);
          pStartPos = -1;
          if (c == '\'') {
            inString = true;
          } else if (c == ':') {
            throw new KJSQLException(ERR_ADJACENT_PARAMS, pStartPos, i);
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
    this.paramMap = convertAndFreeze(tmp, IntList::copyOf);
    this.params = freezeIntoList(tmp.entrySet(), this::toNamedParam);
  }

  String getUnparsedSQL() {
    return unparsed;
  }

  String getNormalizedSQL() {
    return normalized;
  }

  List<NamedParameter> getNamedParameters() {
    return params;
  }

  Map<String, IntList> getParameterMap() {
    return paramMap;
  }

  private static boolean isParamChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private static void addParam(
      Map<String, IntList> paramMap, StringBuilder param, MutableInt pCount, int startPos) {
    if (param.length() == 0) {
      throw new KJSQLException(ERR_EMPTY_NAME, startPos);
    }
    paramMap.computeIfAbsent(param.toString(), k -> new IntArrayList()).add(pCount.ppi());
  }

  private NamedParameter toNamedParam(Entry<String, IntList> e) {
    return new NamedParameter(e.getKey(), IntList.copyOf(e.getValue()));
  }
}
