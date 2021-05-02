package nl.naturalis.yokete.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.check.Check;

public abstract class SQLStatement {

  private final List<Object> bindables = new ArrayList<>(5);

  private final SQL sql;

  SQLStatement(SQL sql) {
    this.sql = sql;
  }

  public void bind(Object bean) {
    Check.notNull(bean).then(bindables::add);
  }

  public void bind(Map<String, Object> map) {
    Check.notNull(map).then(bindables::add);
  }

  public void bind(String param, Object value) {
    Check.notNull(param, "param");
    bindables.add(Collections.singletonMap(param, value));
  }
}
