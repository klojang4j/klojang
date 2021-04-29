package nl.naturalis.yokete.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.util.MutableInt;
import nl.naturalis.yokete.YoketeRuntimeException;

/**
 * Utility class that replaces named parameters in a SQL query with positional parameters and, in
 * the process, produces a list of all parameter names and their position(s) within the query. Note
 * that a named parameter may occur multiple times in a SQL query, for example: {@code SELECT * FROM
 * BOOKS WHERE AUTHOR = :name OR TITLE = :name}.
 */
class ParamExtractor {

  static StatementInfo parseSQL(String sql) {
    StringBuilder out = new StringBuilder(sql);
    Map<String, List<Integer>> params = new HashMap<>();
    MutableInt position = new MutableInt();
    boolean inString = false;
    boolean inParam = false;
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
      } else if (inParam) {
        if (isParamChar(c)) {
          param.append(c);
          if (i == sql.length() - 1) {
            addParam(params, param, position);
          }
        } else {
          addParam(params, param, position);
          out.append(c);
          inParam = false;
          if (c == '\'') {
            inString = true;
          } else if (c == ':') {
            /*
             * Two adjacent parameters ... this can never be valid SQL, so we might as well stop
             * here and throw an exception. But we don't, because we don't want to be a genuine SQL
             * parser.
             */
            out.append('?');
            inParam = true;
            param = new StringBuilder();
          }
        }
      } else if (c == ':') {
        out.append('?');
        inParam = true;
        param = new StringBuilder();
      } else {
        out.append(c);
        if (c == '\'') {
          inString = true;
        }
      }
    }
    return new StatementInfo(out.toString(), Map.copyOf(params));
  }

  private static boolean isParamChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private static void addParam(
      Map<String, List<Integer>> params, StringBuilder param, MutableInt pos) {
    if (param.length() == 0) {
      throw new YoketeRuntimeException("Zero-length parameter found in query string");
    }
    params.computeIfAbsent(param.toString(), k -> new ArrayList<>(4)).add(pos.ppi());
  }
}
