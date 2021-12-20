package org.klojang.template;

import static nl.naturalis.common.StringMethods.concat;

/**
 * Thrown if the path specified in an {@link IncludedTemplatePart included template} could not be
 * resolved to a loadable resource.
 *
 * @author Ayco Holleman
 */
public class PathResolutionException extends ParseException {

  public PathResolutionException(String path) {
    super(concat("Invalid path: \"", path, '"'));
  }
}
