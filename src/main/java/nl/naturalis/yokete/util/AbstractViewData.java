package nl.naturalis.yokete.util;

import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.view.ViewData;

public abstract class AbstractViewData implements ViewData {

  private final ViewDataStringifiers stringifiers;

  public AbstractViewData(ViewDataStringifiers stringifiers) {
    this.stringifiers = stringifiers;
  }

  @Override
  public String getVariableValue(String var) {
    Check.notNull(var);
    Object value = getRawValue(var);
    return stringifiers.stringify(var, value);
  }

  protected abstract Object getRawValue(String var);
}
