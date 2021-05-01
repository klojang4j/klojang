package nl.naturalis.yokete.db;

import java.util.List;

public class SQLStatement {

  public static SQLStatement create(String sql, BindConfig bindConfig) {
    SQLStatementFactory ssf = new SQLStatementFactory(sql);
    return new SQLStatement(ssf.getNormalizedSQL(), ssf.getParams(), bindConfig);
  }

  private final String sql;
  private final List<NamedParameter> params;
  private final BindConfig bindConfig;

  private SQLStatement(String sql, List<NamedParameter> params, BindConfig bindConfig) {
    this.sql = sql;
    this.params = List.copyOf(params);
    this.bindConfig = bindConfig;
  }

  public String getNormalizedSQL() {
    return sql;
  }
}
