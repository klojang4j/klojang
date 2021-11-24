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

  private final TemplateSourceType tst;
  private final PathResolver pathResolver;
  private final Class<?> clazz;
  private final String path;

  TemplateId(TemplateId parentId) {
    this.tst = STRING;
    this.pathResolver = parentId.pathResolver;
    this.clazz = parentId.clazz;
    this.path = null;
  }

  TemplateId() {
    this.tst = STRING;
    this.pathResolver = null;
    this.clazz = null;
    this.path = null;
  }

  TemplateId(String path) {
    this(new File(Check.notNull(path, "path").ok()));
  }

  TemplateId(File file) {
    this.path = Check.notNull(file, "file").ok().getAbsolutePath();
    this.tst = FILE_SYSTEM;
    this.pathResolver = null;
    this.clazz = null;
  }

  TemplateId(Class<?> clazz) {
    Check.notNull(clazz, "clazz");
    this.tst = STRING;
    this.pathResolver = null;
    this.clazz = clazz;
    this.path = null;
  }

  TemplateId(Class<?> clazz, String path) {
    this.clazz = Check.notNull(clazz, "clazz").ok();
    this.path = Check.notNull(path, "path").ok();
    this.tst = RESOURCE;
    this.pathResolver = null;
  }

  TemplateId(PathResolver pathResolver, String path) {
    this.pathResolver = Check.notNull(pathResolver, "pathResolver").ok();
    this.path = Check.notNull(path, "path").ok();
    this.tst = RESOLVER;
    this.clazz = null;
  }

  String getSource() throws InvalidPathException {
    if (path == null) {
      return Check.fail(illegalState(), ERR_NO_PATH, this);
    } else if (tst == FILE_SYSTEM) {
      try {
        return getSource(new FileInputStream(new File(path)));
      } catch (FileNotFoundException e) {
        throw new InvalidPathException(path);
      }
    } else if (tst == RESOURCE) {
      try (InputStream in = clazz.getResourceAsStream(path)) {
        Check.on(InvalidPathException::new, in).is(notNull(), path);
        return IOMethods.toString(in);
      } catch (IOException e) {
        throw new InvalidPathException(path);
      }
    }
    try (InputStream in = pathResolver.resolvePath(path)) {
      Check.on(InvalidPathException::new, in).is(notNull(), path);
      return IOMethods.toString(in);
    } catch (IOException e) {
      throw new InvalidPathException(path);
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
    return tst;
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
    return Objects.hash(clazz, path, tst);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TemplateId other = (TemplateId) obj;
    return tst == other.tst
        && (tst != RESOURCE || clazz.getPackage() == other.clazz.getPackage())
        && Objects.equals(path, other.path);
  }

  public String toString() {
    if (tst == STRING) {
      return concat(
          "TemplateId[sourceType=",
          tst,
          ";package=",
          clazz.getPackage().getName(),
          ";resolver=",
          pathResolver,
          "]");
    }
    if (tst == RESOURCE) {
      return concat(
          "TemplateId[sourceType=",
          tst,
          ";path=",
          path,
          ";package=",
          clazz.getPackage().getName(),
          "]");
    } else if (tst == FILE_SYSTEM) {
      return concat("TemplateId[sourceType=", tst, ";path=", path, "]");
    }
    return concat("TemplateId[sourceType=", tst, ";path=", path, ";resolver=", pathResolver, "]");
  }
}
