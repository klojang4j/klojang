package nl.naturalis.yokete.template;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.integer;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.template.Template.ROOT_TEMPLATE_NAME;

class TemplateCache {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateCache.class);

  private static class TC extends LinkedHashMap<TemplateId, Template> {
    private final int maxCapacity;

    TC(int maxCapacity) {
      super(((maxCapacity * 4) / 3) + 1, 1.0F);
      this.maxCapacity = maxCapacity;
    }

    public boolean removeEldestEntry(Map.Entry<TemplateId, Template> e) {
      if (size() > maxCapacity) {
        if (LOG.isTraceEnabled()) {
          String fmt = "Template {} ({}) evicted from cache";
          LOG.trace(fmt, e.getKey());
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

  Template get(String name, Class<?> clazz, String path) throws ParseException {
    return get(name, new TemplateId(clazz, path));
  }

  Template get(String name, TemplateId id) throws ParseException {
    Check.notNull(name, "name");
    Check.notNull(id, "id");
    Check.that(id.path()).is(notNull());
    LOG.trace("Searching cache for template {} ({})", name, id);
    Template t = cache.get(id);
    if (t == null) {
      LOG.trace("Not found. Parse & cache {}", id);
      t = new Parser(name, id).parse();
      cache.put(id, t);
    } else {
      LOG.trace("Found!");
    }
    return t;
  }
}
