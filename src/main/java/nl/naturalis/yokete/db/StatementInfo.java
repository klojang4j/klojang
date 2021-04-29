package nl.naturalis.yokete.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

class StatementInfo {

  private final String sql;
  private final Map<String, List<Integer>> params;

  StatementInfo(String sql, Map<String, List<Integer>> params) {
    this.sql = sql;
    this.params = params;
  }

  String getSQL() {
    return sql;
  }

  Map<String, List<Integer>> getParams() {
    return params;
  }

  Set<String> getParamNames() {
    return params.keySet();
  }
}
