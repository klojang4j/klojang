package org.klojang.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * The {@code PathResolver} interface enables to define a custom mechanism for loading templates
 * from (likely) persistent storage. Klojang provides two mechanisms: loading templates from the
 * file system and loading templates from the classpath.
 *
 * @see Template#fromResolver(PathResolver, String)
 * @see Template#fromFile(String)
 * @see Template#fromResource(Class, String)
 * @author Ayco Holleman
 */
public interface PathResolver {

  /**
   * Returns an {@code Optional} containing a {@code Boolean} indicating whether the path specified
   * in an {@link IncludedTemplatePart included template} represents a valid resource. If it is
   * expensive to determine this (e.g. requiring a database or SFTP connection) you may return an
   * empty {@code Optional} so that it won't happen twice (once for {@code isValidPath} and once for
   * {@code resolvePath}. If the {@code Optional} <i>does</i> contain a {@code Boolean} this will
   * result in fail-fast behaviour of the template parser.
   *
   * @param path The path to verify
   * @return Whether or not it is a valid path
   */
  Optional<Boolean> isValidPath(String path);

  /**
   * Returns an {@code InputStream} to the resource denoted by the specified path.
   *
   * @param path The path
   * @return An {@code InputStream} to the resource denoted by the specified path
   * @throws IOException If an error occurred while setting up the {@code InputStream}.
   */
  InputStream resolvePath(String path) throws IOException;
}
