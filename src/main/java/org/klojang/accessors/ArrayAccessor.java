package org.klojang.accessors;

import java.util.HashMap;
import java.util.Map;
import org.klojang.render.Accessor;
import org.klojang.render.RenderException;
import org.klojang.template.Template;

public class ArrayAccessor implements Accessor<Object[]> {

  private static final Map<Template, ArrayAccessor> cache = new HashMap<>();

  public static ArrayAccessor getInstance(Template template) {
    return cache.computeIfAbsent(template, ArrayAccessor::new);
  }

  private final Map<String, Integer> router;

  @SuppressWarnings("unchecked")
  private ArrayAccessor(Template template) {
    String[] vars = template.getVariables().toArray(String[]::new);
    Map.Entry<String, Integer>[] entries = new Map.Entry[vars.length];
    for (int i = 0; i < vars.length; ++i) {
      entries[i] = Map.entry(vars[i], i);
    }
    router = Map.ofEntries(entries);
  }

  @Override
  public Object access(Object[] data, String property) throws RenderException {
    return data[router.get(property)];
  }
}
