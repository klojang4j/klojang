package nl.naturalis.yokete.template;

import java.util.HashMap;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.NumberMethods;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gte;
import static nl.naturalis.common.check.CommonChecks.integer;
import static nl.naturalis.yokete.template.Template.ROOT_TEMPLATE_NAME;

class TemplateCache {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateCache.class);
  private static final String ERR_CACHE_SIZE = "Illegal value specified for %s: %s";

  static final TemplateCache INSTANCE = new TemplateCache();

  private final HashMap<String, Template> cache;
  private final LinkedList<String> entries;
  private final int maxSize;

  private TemplateCache() {
    String k = "org.klojang.template.cacheSize";
    String v = System.getProperty(k, "-1");
    maxSize = Check.that(v, k).is(integer(), ERR_CACHE_SIZE, v).ok(NumberMethods::parseInt);
    String s = maxSize == 0 ? " (caching disabled)" : maxSize == -1 ? " (unlimited)" : "";
    LOG.trace("Template cache size: {}{}", maxSize, s);
    Check.that(maxSize).is(gte(), -1, ERR_CACHE_SIZE, k, maxSize);
    if (maxSize == 0) {
      cache = null;
      entries = null;
    } else if (maxSize == -1) {
      cache = new HashMap<>(32);
      entries = null;
    } else {
      cache = new HashMap<>(maxSize * (int) Math.ceil(10 / 7) + 1, (float) 7 / 10);
      entries = new LinkedList<>();
    }
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
    if (maxSize == 0 || id.path() == null) { // caching disabled
      return new Parser(name, id).parse();
    }
    LOG.trace("Searching cache for template {}:{}", name, id.path());
    Template t = cache.get(id.path());
    if (t == null) {
      LOG.trace("Not found. Parse & cache {}", id.path());
      t = new Parser(name, id).parse();
      if (maxSize != -1 && entries.size() >= maxSize) {
        String eldest = entries.pop();
        LOG.trace("Cache overflow. Evicting {}", eldest);
        cache.remove(eldest);
        entries.add(id.path());
      }
      cache.put(id.path(), t);
    } else {
      LOG.trace("Found!");
    }
    return t;
  }
}
