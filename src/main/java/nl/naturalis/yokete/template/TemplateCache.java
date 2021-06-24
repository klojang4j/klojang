package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.HashMap;
import nl.naturalis.common.Tuple;

import static nl.naturalis.yokete.template.Template.ROOT_TEMPLATE_NAME;

class TemplateCache {

  static final TemplateCache INSTANCE = new TemplateCache();

  private final HashMap<Tuple<Object, String>, Template> cache = new HashMap<>();

  private TemplateCache() {}

  Template get(Class<?> clazz, String path) throws ParseException {
    return get(ROOT_TEMPLATE_NAME, clazz, path);
  }

  Template get(String tmplName, Class<?> clazz, String path) throws ParseException {
    Tuple<Object, String> key = Tuple.of(clazz.getPackage(), path);
    Template t = cache.get(key);
    if (t == null) {
      t = new Parser(tmplName, clazz, Path.of(path)).parse();
      cache.put(key, t);
    }
    return t;
  }
}
