package org.klojang.render;

import org.klojang.db.Row;

/**
 * Generic name mapping interface. Name mappers are used to map template variable names to model
 * object properties. See {@link AccessorFactory.Builder#setDefaultNameMapper(NameMapper)}. They are
 * also used to map column names to model object properties. See {@link
 * SQLQuery#withNameMapper(NameMapper)}. Note that the term "property" is in fact rather misleading
 * because, as for Klojang, your model objects might just as well be {@code Map<String,Object>}
 * objects or {@link Row rows}, in which case your template's variables would map to map keys.
 *
 * @author Ayco Holleman
 */
@FunctionalInterface
public interface NameMapper {

  /** The no-op mapper. Maps the variable or nested template name to itself. */
  public static NameMapper NOOP = x -> x;

  /**
   * Maps the specified name to a name that can be used to access its value.
   *
   * @param template The template containing the variable or nested template
   * @param name The name of the variable or nested template
   * @return A (new) name that can be used to access the value
   */
  String map(String name);
}
