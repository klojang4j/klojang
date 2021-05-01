package nl.naturalis.yokete.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.invoke.BeanReader;
import nl.naturalis.common.invoke.Getter;
import nl.naturalis.common.invoke.GetterFactory;
import nl.naturalis.yokete.db.write.ValuePersister;
import nl.naturalis.yokete.db.write.PersisterNegotiator;
import static nl.naturalis.common.check.CommonChecks.in;

public class BeanBinder<T> {

  private final BeanValueTransporter<?, ?>[] transporters;

  public BeanBinder(Class<T> beanClass, List<NamedParameter> params, BindConfig cfg) {
    transporters = BeanValueTransporter.createTransporters(beanClass, params, cfg);
  }
}
