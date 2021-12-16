package org.klojang.template;

import java.io.*;
import java.util.Objects;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.IOMethods;
import nl.naturalis.common.check.Check;
import static org.klojang.template.TemplateSourceType.RESOLVER;
import static org.klojang.template.TemplateSourceType.FILE_SYSTEM;
import static org.klojang.template.TemplateSourceType.RESOURCE;
import static org.klojang.template.TemplateSourceType.STRING;
import static nl.naturalis.common.StringMethods.concat;
import static nl.naturalis.common.check.CommonChecks.illegalState;
import static nl.naturalis.common.check.CommonChecks.notNull;

public class TemplateId {

  private static final String ERR_NO_PATH = "Cannot load source for %s";

  private final TemplateSourceType sourceType;
  private final PathResolver pathResolver;
  private final Class<?> clazz;
  private final String path;

  TemplateId(TemplateId parentId) {
    this.sourceType = STRING;
    this.pathResolver = parentId.pathResolver;
    this.clazz = parentId.clazz;
    this.path = null;
  }

  TemplateId() {
    this.sourceType = STRING;
    this.pathResolver = null;
    this.clazz = null;
    this.path = null;
  }

  TemplateId(String path) {
    this(new File(Check.notNull(path, "path").ok()));
  }

  TemplateId(File file) {
    this.path = Check.notNull(file, "file").ok().getAbsolutePath();
    this.sourceType = FILE_SYSTEM;
    this.pathResolver = null;
    this.clazz = null;
  }

  TemplateId(Class<?> clazz) {
    Check.notNull(clazz, "clazz");
    this.sourceType = STRING;
    this.pathResolver = null;
    this.clazz = clazz;
    this.path = null;
  }

  TemplateId(Class<?> clazz, String path) {
    this.clazz = Check.notNull(clazz, "clazz").ok();
    this.path = Check.notNull(path, "path").ok();
    this.sourceType = RESOURCE;
    this.pathResolver = null;
  }

  TemplateId(PathResolver pathResolver, String path) {
    this.pathResolver = Check.notNull(pathResolver, "pathResolver").ok();
    this.path = Check.notNull(path, "path").ok();
    this.sourceType = RESOLVER;
    this.clazz = null;
  }

  String getSource() throws PathResolutionException {
    if (path == null) {
      return Check.fail(illegalState(), ERR_NO_PATH, this);
    } else if (sourceType == FILE_SYSTEM) {
      try {
        return getSource(new FileInputStream(new File(path)));
      } catch (FileNotFoundException e) {
        throw new PathResolutionException(path);
      }
    } else if (sourceType == RESOURCE) {
      try (InputStream in = clazz.getResourceAsStream(path)) {
        Check.on(PathResolutionException::new, in).is(notNull(), path);
        return IOMethods.toString(in);
      } catch (IOException e) {
        throw new PathResolutionException(path);
      }
    }
    try (InputStream in = pathResolver.resolvePath(path)) {
      Check.on(PathResolutionException::new, in).is(notNull(), path);
      return IOMethods.toString(in);
    } catch (IOException e) {
      throw new PathResolutionException(path);
    }
  }

  private static String getSource(InputStream in) {
    try (in) {
      return IOMethods.toString(in);
    } catch (IOException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  TemplateSourceType sourceType() {
    return sourceType;
  }

  PathResolver pathResolver() {
    return pathResolver;
  }

  Class<?> clazz() {
    return clazz;
  }

  String path() {
    return path;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, path, sourceType);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TemplateId other = (TemplateId) obj;
    return sourceType == other.sourceType
        && (sourceType != RESOURCE || clazz.getPackage() == other.clazz.getPackage())
        && Objects.equals(path, other.path);
  }

  public String toString() {
    if (sourceType == STRING) {
      return concat(
          "TemplateId[sourceType=",
          sourceType,
          ";package=",
          clazz.getPackage().getName(),
          ";resolver=",
          pathResolver,
          "]");
    }
    if (sourceType == RESOURCE) {
      return concat(
          "TemplateId[sourceType=",
          sourceType,
          ";path=",
          path,
          ";package=",
          clazz.getPackage().getName(),
          "]");
    } else if (sourceType == FILE_SYSTEM) {
      return concat("TemplateId[sourceType=", sourceType, ";path=", path, "]");
    }
    return concat("TemplateId[sourceType=", sourceType, ";path=", path, ";resolver=", pathResolver, "]");
  }
}
