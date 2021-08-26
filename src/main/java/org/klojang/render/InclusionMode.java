package org.klojang.render;

/**
 * Used to specify whether you want or don't want to populate a set of variables and nested
 * templates.
 *
 * @author Ayco Holleman
 */
public enum InclusionMode {
  /** Only set/populate the subsequently specified variables and nested templates. */
  ONLY,
  /** Set/populate all variables and nested templates except those subsequently specified. */
  EXCEPT;
}
