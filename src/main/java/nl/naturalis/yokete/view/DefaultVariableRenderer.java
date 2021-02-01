package nl.naturalis.yokete.view;

import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.Tuple;

public class DefaultVariableRenderer implements VariableRenderer {

  private final Map<Tuple<String, Class<?>>, VariableRenderer> namedStringifiers = new HashMap<>();
  private final Map<Class<?>, VariableRenderer> typedStringifiers = new HashMap<>();

  @Override
  public String apply(String varName, Object value) {
    final Tuple<String, Class<?>> key = Tuple.of(varName, value.getClass());
    VariableRenderer vr = namedStringifiers.get(key);
    if (key == null) {
      vr = typedStringifiers.get(value.getClass());
      if (vr == null) {
        for (Tuple<String, Class<?>> t : namedStringifiers.keySet()) {
          if (t.getRight().isInstance(value)) {
            vr = namedStringifiers.get(t);
            namedStringifiers.put(key, vr);
            break;
          }
        }
        if (vr == null) {
          for (Class<?> c : typedStringifiers.keySet()) {
            if (c.isInstance(value)) {
              vr = typedStringifiers.get(c);
              typedStringifiers.put(value.getClass(), vr);
              break;
            }
          }
          if (vr == null) {
            return value.toString();
          }
        }
      }
    }
    return vr.apply(varName, value);
  }
}
