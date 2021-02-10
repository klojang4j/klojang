package nl.naturalis.yokete.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.view.RenderException;
import nl.naturalis.yokete.view.Template;
import nl.naturalis.yokete.view.ViewData;
import static nl.naturalis.common.CollectionMethods.asList;
import static nl.naturalis.common.check.CommonChecks.notNull;

public abstract class AbstractViewData implements ViewData {

  /**
   * Special value to return from {@link #getRawValue(Template, String) getRawValue(template, name)}
   * if the name does not map to a property in the source data.
   */
  protected static final Optional<?> NULL = Optional.of(new Object());

  protected final ViewDataStringifiers stringifiers;

  public AbstractViewData(ViewDataStringifiers stringifiers) {
    this.stringifiers = stringifiers;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Optional<List<String>> getValue(Template template, String varName) throws RenderException {
    Check.notNull(varName);
    Optional raw = getRawValue(template, varName);
    if (raw.isEmpty()) {
      return raw;
    }
    List rawValues = asList(raw.get());
    List<String> strValues = new ArrayList<>(rawValues.size());
    for (Object obj : rawValues) {
      strValues.add(stringifiers.stringify(varName, obj));
    }
    return Optional.of(strValues);
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Optional<List<ViewData>> getNestedViewData(Template template, String tmplName)
      throws RenderException {
    Check.notNull(tmplName);
    Optional raw = getRawValue(template, tmplName);
    if (raw.isEmpty()) {
      return raw;
    }
    List objects = asList(raw.get());
    List<ViewData> viewData = new ArrayList<>(objects.size());
    for (int i = 0; i < objects.size(); ++i) {
      Check.with(RenderException::new, objects.get(i))
          .is(notNull(), "Cannot create ViewData from null value at index %d", i)
          .then(obj -> viewData.add(createViewData(template, tmplName, obj)));
    }
    return Optional.of(viewData);
  }

  /**
   * Returns the untyped value associated with the specified name, which can be the name of a
   * template variable or a nested template. If the name does not map to any property in the source
   * data, an empty {@code Optional} must be returned. However, if the name <i>does</i> map to a
   * property in the source data, but the property's value turns out to be null, the special value
   * {@link #NULL} must be returned.
   *
   * @param template
   * @param name
   * @return
   */
  protected abstract Optional<?> getRawValue(Template template, String name) throws RenderException;

  /**
   * Creates a {@code ViewData} object from the specified value. The value may be the raw value
   * returned from {@link #getRawValue(Template, String)} or, if the raw value was multi-valued
   * (e.g. an array), one element from it. The value is anyhow guaranteed not to be {@code null}.
   *
   * @param template
   * @param name
   * @param obj
   * @return
   */
  protected abstract ViewData createViewData(Template template, String name, Object value)
      throws RenderException;
}
