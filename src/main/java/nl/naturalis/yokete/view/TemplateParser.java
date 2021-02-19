package nl.naturalis.yokete.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.IOMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.internal.VisibleForTesting;
import static nl.naturalis.common.StringMethods.substrAfter;
import static nl.naturalis.common.StringMethods.substrTo;
import static nl.naturalis.common.check.CommonChecks.notBlank;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.view.InvalidTemplateException.*;
import static nl.naturalis.yokete.view.Regex.*;

class TemplateParser {

  private final String src;
  private final Class<?> clazz;

  TemplateParser(String src, Class<?> clazz) {
    this.src = Check.notNull(src).ok();
    this.clazz = clazz;
  }

  TemplateParser(Class<?> clazz, String path) throws InvalidTemplateException {
    this.clazz = Check.notNull(clazz).ok();
    try (InputStream in = clazz.getResourceAsStream(path)) {
      Check.on(invalidPath(clazz, path), in).is(notNull());
      this.src = IOMethods.toString(in);
    } catch (IOException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  List<Part> parse() throws InvalidTemplateException {
    String src = this.src;
    src = removeDitchBlocks(src);
    src = uncommentVariables(src);
    src = uncommentTemplates(src);
    src = uncommentIncludes(src);
    LinkedList<Part> parts = new LinkedList<>();
    // Gets populated with the names of nested and imported templates, so we can check for
    // duplicates across methods:
    Set<String> names = new HashSet<>();
    parseNestedTemplates(src, parts, names);
    parseImportedTemplates(parts, names);
    parseVariables(parts, names);
    collectTextParts(parts);
    return List.copyOf(parts);
  }

  @VisibleForTesting
  void parseNestedTemplates(String src, LinkedList<Part> parts, Set<String> names)
      throws InvalidTemplateException {
    Matcher matcher = REGEX_TEMPLATE.matcher(src);
    int end = 0; // end index of the previous nested template
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String unparsed = src.substring(end, start);
        parts.add(new UnparsedPart(unparsed, end, start));
      }
      String name = matcher.group(1);
      Check.on(emptyTemplateName(start), name).is(notBlank());
      Check.on(duplicateTemplateName(name, start), name).is(notIn(), names);
      names.add(name);
      String nestedSrc = matcher.group(2);
      end = matcher.end();
      Template nested = Template.parse(name, nestedSrc, clazz);
      parts.add(new TemplatePart(nested, start, end));
    }
    if (end < src.length()) {
      String unparsed = src.substring(end);
      parts.add(new UnparsedPart(unparsed, end, src.length()));
    }
  }

  private void parseImportedTemplates(LinkedList<Part> parseResult, Set<String> names)
      throws InvalidTemplateException {
    for (int i = 0; i < parseResult.size(); ++i) {
      Part p = parseResult.get(i);
      if (p.getClass() == UnparsedPart.class) {
        List<Part> parts = parseImportedTemplates((UnparsedPart) p, names);
        parseResult.remove(i);
        parseResult.addAll(i, parts);
      }
    }
  }

  private static void parseVariables(LinkedList<Part> parseResult, Set<String> names)
      throws InvalidTemplateException {
    for (int i = 0; i < parseResult.size(); ++i) {
      Part p = parseResult.get(i);
      if (p.getClass() == UnparsedPart.class) {
        // Further divide the part into variable parts and text parts
        List<Part> parts = parseVariables((UnparsedPart) p, names);
        parseResult.remove(i);
        parseResult.addAll(i, parts);
      }
    }
  }

  /* Text parts are all unparsed parts that remain after everything else has been parsed out */
  private static void collectTextParts(LinkedList<Part> parseResult) {
    for (int i = 0; i < parseResult.size(); ++i) {
      if (parseResult.get(i).getClass() == UnparsedPart.class) {
        UnparsedPart unparsed = (UnparsedPart) parseResult.get(i);
        parseResult.set(i, unparsed.toTextPart());
      }
    }
  }

  /*
   * Extracts import declarations, which look like this:
   *
   * ~%%import:template_name:/path/to/template_file%
   *
   * or like this:
   *
   * ~%%import:/path/to/template_file%
   *
   * In the latter case, the base name of the file is taken to be the name of
   * the template
   */
  @VisibleForTesting
  List<Part> parseImportedTemplates(UnparsedPart unparsed, Set<String> names)
      throws InvalidTemplateException {
    String src = unparsed.getContents();
    int offset = unparsed.start();
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_INCLUDE.matcher(src);
    int end = 0;
    while (matcher.find()) {
      int start = matcher.start();
      int absStart = start + offset;
      if (start > end) {
        parts.add(parseLater(unparsed, end, start));
      }
      String name = matcher.group(2);
      String path = matcher.group(3);
      Check.on(emptyPath(absStart), path).is(notBlank());
      if (name == null) {
        name = substrTo(substrAfter(path, "/", true), '.');
      }
      Check.on(emptyTemplateName(absStart), name).is(notBlank());
      Check.on(duplicateTemplateName(name, absStart), name).is(notIn(), names);
      Check.on(missingClassObject(name, path, absStart), clazz).is(notNull());
      names.add(name);
      try (InputStream in = clazz.getResourceAsStream(path)) {
        Check.on(invalidImportPath(clazz, path, absStart), in).is(notNull());
        String contents = IOMethods.toString(in);
        Template imported = Template.parse(name, contents, clazz);
        parts.add(new TemplatePart(imported, absStart, end + offset));
      } catch (IOException e) {
        throw ExceptionMethods.uncheck(e);
      }
    }
    if (end < src.length()) {
      parts.add(parseLater(unparsed, end, src.length()));
    }
    return parts;
  }

  @VisibleForTesting
  static List<Part> parseVariables(UnparsedPart unparsed, Set<String> names)
      throws InvalidTemplateException {
    String src = unparsed.getContents();
    int offset = unparsed.start();
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_VARIABLE.matcher(src);
    int end = 0;
    EscapeType escType;
    while (matcher.find()) {
      int start = matcher.start();
      int absStart = start + offset;
      if (start > end) {
        parts.add(parseLater(unparsed, end, start));
      }
      String esc = matcher.group(2);
      String name = matcher.group(3);
      escType = Check.catching(badEscapeType(esc, absStart), esc, EscapeType::parse).ok();
      Check.on(emptyVarName(absStart), name).is(notBlank());
      Check.on(duplicateVarName(name, absStart), name).is(notIn(), names);
      end = matcher.end();
      parts.add(new VariablePart(escType, name, absStart, end + offset));
    }
    if (end < src.length()) {
      parts.add(parseLater(unparsed, end, src.length()));
    }
    return parts;
  }

  /*
   * Replaces
   *
   * <!-- ~%myVar% -->
   *
   * with
   *
   * ~%myVar%
   *
   * after which there is no longer any difference between "hidden" and "visible"
   * variables, and they can all be processed identically
   */
  private static String uncommentVariables(String template) {
    Matcher matcher = REGEX_HIDDEN_VAR.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String uncommentTemplates(String template) {
    Matcher matcher = REGEX_HIDDEN_TMPL.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String uncommentIncludes(String template) {
    Matcher matcher = REGEX_HIDDEN_INCLUDE.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String removeDitchBlocks(String template) {
    template = REGEX_DITCH_BLOCK.matcher(template).replaceAll("");
    return template;
  }

  private static UnparsedPart parseLater(UnparsedPart p, int from, int to) {
    String s = p.getContents().substring(from, to);
    return new UnparsedPart(s, from + p.start(), to + p.start());
  }
}
