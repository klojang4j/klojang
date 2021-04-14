package nl.naturalis.yokete.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.YoketeRuntimeException;
import nl.naturalis.yokete.accessors.MapAccessor;
import nl.naturalis.yokete.render.*;
import nl.naturalis.yokete.template.ParseException;
import nl.naturalis.yokete.template.Template;
import static nl.naturalis.common.ObjectMethods.ifNotNull;
import static nl.naturalis.common.check.CommonChecks.gt;
import static nl.naturalis.common.check.CommonChecks.keyIn;
import static nl.naturalis.common.check.CommonChecks.notNull;
import static nl.naturalis.yokete.render.TemplateStringifiers.BASIC;

public class ListBoxHtmlHelper<T> {

  private static final String ERR_BAD_KEY = "ListBoxHtmlHelper with key \"%s\" does not exist";
  private static final String ERR_DUP_KEY = "ListBoxHtmlHelper with key \"%s\" already created";
  private static final String ERR_NO_CACHE =
      "No ListBoxHtmlHelper instances available. Did you close the Factory?";
  private static final String NOT_SET = new String();

  // While a Factory is alive we lock the entire ListBoxHtmlHelper class
  // so all threads always see the same ListBoxHtmlHelper instances.
  private static final ReentrantLock LOCK = new ReentrantLock();

  private static Map<String, ListBoxHtmlHelper<?>> CACHE;

  private static Template TEMPLATE;

  /* ++++++++++++++++++++[ BEGIN FACTORY CLASS ]+++++++++++++++++ */

  public static class Factory implements AutoCloseable {

    private final Map<String, ListBoxHtmlHelper<?>> tmpCache;

    private Accessor<?> accessor = new MapAccessor();
    private String initOption = NOT_SET;
    private String initVal = NOT_SET;

    private Factory() {
      tmpCache = ifNotNull(CACHE, HashMap::new, new HashMap<>());
      CACHE = null;
    }

    public Factory setAccessor(Accessor<?> accessor) {
      this.accessor = Check.notNull(accessor).ok();
      return this;
    }

    public Factory setInitOption(String initOption) {
      this.initOption = Check.notNull(initOption).ok();
      return this;
    }

    public Factory setInitVal(String initVal) {
      this.initVal = Check.notNull(initVal).ok();
      return this;
    }

    public <U> Factory addHelper(String name, Supplier<List<U>> dataSupplier) {
      return addHelper(name, name, dataSupplier);
    }

    public <U> Factory addHelper(String key, String name, Supplier<List<U>> dataSupplier) {
      Check.that(key).is(notNull()).isNot(keyIn(), CACHE, ERR_DUP_KEY, key);
      Check.notNull(name, "name");
      Check.notNull(dataSupplier, "dataSupplier");
      @SuppressWarnings("unchecked")
      ListBoxHtmlHelper<U> helper =
          new ListBoxHtmlHelper<>(
              key, name, dataSupplier, (Accessor<U>) accessor, initOption, initVal);
      tmpCache.put(key, helper);
      CACHE = Map.copyOf(tmpCache);
      return this;
    }

    @Override
    public void close() {
      try {
        Check.that(tmpCache.size()).is(gt(), 0, "At least one ListBoxHtmlHelper required");
        CACHE = Map.copyOf(tmpCache);
      } finally {
        LOCK.unlock();
      }
    }
  }

  /* ++++++++++++++++++++[ END FACTORY CLASS ]+++++++++++++++++ */

  public static Factory getFactory() {
    LOCK.lock();
    return new Factory();
  }

  public static ListBoxHtmlHelper<?> getHelper(String key) {
    Check.that(CACHE).is(notNull(), ERR_NO_CACHE);
    return Check.that(key).is(keyIn(), CACHE, ERR_BAD_KEY, key).ok(CACHE::get);
  }

  private final String key;
  private final String name;
  private final Supplier<List<T>> dataSupplier;
  private final Accessor<T> accessor;
  private final String initOption;
  private final String initVal;

  private Renderable renderable;

  private ListBoxHtmlHelper(
      String key,
      String name,
      Supplier<List<T>> dataSupplier,
      Accessor<T> accessor,
      String initOption,
      String initVal) {
    this.key = key;
    this.name = name;
    this.dataSupplier = dataSupplier;
    this.accessor = accessor;
    this.initOption = initOption;
    this.initVal = initVal;
  }

  public synchronized void invalidate() {
    renderable = null;
  }

  public synchronized Renderable getRenderable() {
    if (renderable == null) {
      renderable = refresh();
    }
    return renderable;
  }

  private Renderable refresh() {
    try {
      Page page = Page.configure(getTemplate(), (x, y) -> accessor, BASIC);
      RenderSession session = page.newRenderSession();
      session.set("name", name);
      session.set("YLB_KEY", key);
      session.fill("options", dataSupplier.get());
      if (initOption != NOT_SET) {
        session.fillTupleTemplate("initOption", List.of(Tuple.of(initVal, initOption)));
      }
      return session.createRenderable();
    } catch (ParseException | RenderException e) {
      throw new YoketeRuntimeException(e);
    }
  }

  private static Template getTemplate() throws ParseException {
    if (TEMPLATE == null) {
      TEMPLATE = Template.parse("ListBox.html");
    }
    return TEMPLATE;
  }
}
