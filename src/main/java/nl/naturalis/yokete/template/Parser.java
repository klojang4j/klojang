package nl.naturalis.yokete.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.IOMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.internal.VisibleForTesting;
import nl.naturalis.yokete.view.EscapeType;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.template.ParseException.*;
import static nl.naturalis.yokete.template.Regex.*;

class Parser {

  private final String tmplName;
  private final Class<?> clazz;
  private final Path path;
  private final String src;

  Parser(String tmplName, Class<?> clazz, String src) {
    this.tmplName = tmplName;
    this.path = null;
    this.src = Check.notNull(src).ok();
    this.clazz = clazz;
  }

  Parser(String tmplName, Class<?> clazz, Path path) throws ParseException {
    this.tmplName = tmplName;
    this.clazz = Check.notNull(clazz).ok();
    this.path = path;
    try (InputStream in = clazz.getResourceAsStream(path.toString())) {
      Check.on(invalidPath(path), in).is(notNull());
      this.src = IOMethods.toString(in);
    } catch (IOException e) {
      throw ExceptionMethods.uncheck(e);
    }
  }

  Template parse() throws ParseException {
    String src = this.src;
    src = removeDitchBlocks(src);
    src = uncommentVariables(src);
    src = uncommentTemplates(src);
    src = uncommentIncludes(src);
    // Gets populated with the names of nested and imported templates, so we can check for
    // duplicates across methods:
    Set<String> names = new HashSet<>();
    List<Part> parts = parseNestedTemplates(src, names);
    parts = parseIncludedTemplates(parts, names);
    parts = parseVariables(parts, names);
    parts = collectTextParts(parts);
    return new Template(tmplName, path, List.copyOf(parts));
  }

  private List<Part> parseNestedTemplates(String src, Set<String> names) throws ParseException {
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_NESTED.matcher(src);
    int end = 0; // end index of the previous nested template
    while (matcher.find()) {
      int start = matcher.start();
      if (start > end) {
        String unparsed = src.substring(end, start);
        parts.add(new UnparsedPart(unparsed, end, start));
      }
      String name = matcher.group(1);
      Check.on(emptyTemplateName(this.src, start), name).is(notBlank());
      Check.on(duplicateTemplateName(this.src, start, name), name).is(notIn(), names);
      names.add(name);
      String mySrc = matcher.group(2);
      end = matcher.end();
      Template nested = Template.parse(name, clazz, mySrc);
      parts.add(new NestedTemplatePart(nested, start, end));
    }
    if (end < src.length()) {
      String unparsed = src.substring(end);
      parts.add(new UnparsedPart(unparsed, end, src.length()));
    }
    return parts;
  }

  private List<Part> parseIncludedTemplates(List<Part> in, Set<String> names)
      throws ParseException {
    List<Part> out = new ArrayList<>(in.size() + 10);
    for (int i = 0; i < in.size(); ++i) {
      Part p = in.get(i);
      if (p.getClass() == UnparsedPart.class) {
        out.addAll(parseIncludedTemplates((UnparsedPart) p, names));
      } else {
        out.add(p);
      }
    }
    return out;
  }

  private List<Part> parseVariables(List<Part> in, Set<String> names) throws ParseException {
    List<Part> out = new ArrayList<>(in.size() + 20);
    for (int i = 0; i < in.size(); ++i) {
      Part p = in.get(i);
      if (p.getClass() == UnparsedPart.class) {
        out.addAll(parseVariables((UnparsedPart) p, names));
      } else {
        out.add(p);
      }
    }
    return out;
  }

  /* Text parts are all unparsed parts that remain after everything else has been parsed out */
  private List<Part> collectTextParts(List<Part> in) throws ParseException {
    for (int i = 0; i < in.size(); ++i) {
      if (in.get(i).getClass() == UnparsedPart.class) {
        UnparsedPart up = (UnparsedPart) in.get(i);
        checkGarbage(up);
        in.set(i, up.toTextPart());
      }
    }
    return in;
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
  private List<Part> parseIncludedTemplates(UnparsedPart unparsed, Set<String> names)
      throws ParseException {
    String src = unparsed.getContents();
    int offset = unparsed.start();
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_INCLUDE.matcher(src);
    int end = 0;
    while (matcher.find()) {
      int start = matcher.start();
      int absStart = offset + start;
      if (start > end) {
        parts.add(todo(unparsed, end, start));
      }
      String name = matcher.group(2);
      String path = matcher.group(3);
      Check.on(emptyPath(this.src, absStart), path).is(notBlank());
      if (name == null) {
        name = IncludedTemplatePart.extractName(path);
      }
      Check.on(emptyTemplateName(this.src, absStart), name).is(notBlank());
      Check.on(duplicateTemplateName(this.src, absStart, name), name).is(notIn(), names);
      Check.on(missingClassObject(this.src, absStart, name, path), clazz).is(notNull());
      names.add(name);
      end = matcher.end();
      Template t = Template.parse(name, clazz, Path.of(path));
      parts.add(new IncludedTemplatePart(t, absStart, offset + end));
    }
    if (end < src.length()) {
      parts.add(todo(unparsed, end, src.length()));
    }
    return parts;
  }

  @VisibleForTesting
  List<Part> parseVariables(UnparsedPart unparsed, Set<String> names) throws ParseException {
    String src = unparsed.getContents();
    int offset = unparsed.start();
    List<Part> parts = new ArrayList<>();
    Matcher matcher = REGEX_VARIABLE.matcher(src);
    int end = 0;
    EscapeType escType;
    while (matcher.find()) {
      int start = matcher.start();
      int absStart = offset + start;
      if (start > end) {
        parts.add(todo(unparsed, end, start));
      }
      String esc = matcher.group(2);
      String name = matcher.group(3);
      escType = Check.catching(badEscapeType(this.src, absStart, esc), esc, EscapeType::parse).ok();
      Check.on(emptyVarName(this.src, absStart), name).is(notBlank());
      Check.on(duplicateVarName(this.src, absStart, name), name).is(notIn(), names);
      end = matcher.end();
      parts.add(new VariablePart(escType, name, absStart, offset + end));
    }
    if (end < src.length()) {
      parts.add(todo(unparsed, end, src.length()));
    }
    return parts;
  }

  private void checkGarbage(UnparsedPart unparsed) throws ParseException {
    String str = unparsed.getContents();
    int off = unparsed.start();
    Matcher m0 = REGEX_DITCH_TOKEN.matcher(str);
    Check.on(s -> ditchBlockNotTerminated(src, off + m0.start()), m0.find()).is(no());
    Matcher m1 = REGEX_TMPL_END.matcher(str);
    Check.on(s -> danglingEndOfTemplate(src, off + m1.start()), m1.find()).is(no());
    int idx = str.indexOf(TMPL_START + "begin:");
    Check.on(templateNotTerminated(src, off + idx), idx).is(eq(), -1);
    idx = str.indexOf(TMPL_START + "end:");
    Check.on(templateNotTerminated(src, off + idx), idx).is(eq(), -1);
    idx = str.indexOf(TMPL_START + "include:");
    Check.on(includeNotTerminated(src, off + idx), idx).is(eq(), -1);
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
    Matcher matcher = REGEX_VARIABLE_CMT.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String uncommentTemplates(String template) {
    Matcher matcher = REGEX_NESTED_CMT.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String uncommentIncludes(String template) {
    Matcher matcher = REGEX_INCLUDE_CMT.matcher(template);
    template = matcher.replaceAll(r -> r.group(1));
    return template;
  }

  private static String removeDitchBlocks(String template) {
    template = REGEX_DITCH_BLOCK.matcher(template).replaceAll("");
    return template;
  }

  private static UnparsedPart todo(UnparsedPart p, int from, int to) {
    String s = p.getContents().substring(from, to);
    return new UnparsedPart(s, from + p.start(), to + p.start());
  }
}
