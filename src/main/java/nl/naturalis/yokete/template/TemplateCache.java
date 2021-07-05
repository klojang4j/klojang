package nl.naturalis.yokete.template;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.integer;
import static nl.naturalis.yokete.template.Template.ROOT_TEMPLATE_NAME;
import static nl.naturalis.yokete.template.TemplateUtils.*;

class TemplateCache {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateCache.class);

  private static class TC extends LinkedHashMap<Tuple<Package, String>, Template> {
    private final int maxCapacity;

    TC(int maxCapacity) {
      super(((maxCapacity * 4) / 3) + 1, 1.0F);
      this.maxCapacity = maxCapacity;
    }

    public boolean removeEldestEntry(Map.Entry<Tuple<Package, String>, Template> e) {
      if (size() > maxCapacity) {
        if (LOG.isTraceEnabled()) {
          String fmt = "Template {} ({}) evicted from cache";
          LOG.trace(fmt, getFQName(e.getValue()), e.getKey().getRight());
        }
        return true;
      }
      return false;
    }
  }

  static final TemplateCache INSTANCE = new TemplateCache();

  private final TC cache;

  private TemplateCache() {
    String prop = "org.klojang.template.cacheSize";
    String val = System.getProperty(prop, "50");
    int i = Check.that(val, prop).is(integer()).ok(NumberMethods::parseInt);
    cache = new TC(i);
  }

  Template get(Class<?> clazz, String path) throws ParseException {
    return get(ROOT_TEMPLATE_NAME, clazz, path);
  }

  Template get(String tmplName, Class<?> clazz, String path) throws ParseException {
    Tuple<Package, String> key = Tuple.of(clazz.getPackage(), path);
    LOG.trace("Searching cache for template {} ({})", tmplName, path);
    Template t = cache.get(key);
    if (t == null) {
      LOG.trace("Not found. Parse & cache {}", path);
      t = new Parser(tmplName, clazz, Path.of(path)).parse();
      cache.put(key, t);
    } else {
      LOG.trace("Found!");
    }
    return t;
  }
}
