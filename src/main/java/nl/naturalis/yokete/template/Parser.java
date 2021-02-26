package nl.naturalis.yokete.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.IOMethods;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.function.ThrowingBiFunction;
import nl.naturalis.yokete.view.EscapeType;
import static nl.naturalis.common.check.CommonChecks.*;
import static nl.naturalis.yokete.template.ParseException.*;
import static nl.naturalis.yokete.template.Regex.*;

class Parser {

  private static interface PartialParser
      extends ThrowingBiFunction<UnparsedPart, Set<String>, List<Part>, ParseException> {}

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
    List<Part> parts = purgeDitchBlocks(src);
    // Accumulates template names for duplicate checks:
    Set<String> names = new HashSet<>();
    parts = uncomment(parts, REGEX_NESTED_CMT);
    parts = uncomment(parts, REGEX_INCLUDE_CMT);
    parts = uncomment(parts, REGEX_VARIABLE_CMT);
    parts = parse(parts, names, this::parseNested);
    parts = parse(parts, names, this::parseIncludes);
    parts = parse(parts, names, this::parseVariables);
    parts = collectTextParts(parts);
    return new Template(tmplName, path, List.copyOf(parts));
  }

  @SuppressWarnings("static-method")
  private List<Part> parse(List<Part> in, Set<String> names, PartialParser parser)
      throws ParseException {
    List<Part> out = new ArrayList<>(in.size() + 20);
    for (Part p : in) {
      if (p.getClass() == UnparsedPart.class) {
        out.addAll(parser.apply((UnparsedPart) p, names));
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

  private List<Part> parseNested(UnparsedPart raw, Set<String> names) throws ParseException {
    List<Part> parts = new ArrayList<>();
    Matcher m = REGEX_NESTED.matcher(raw.text());
    int offset = raw.start(), end = 0;
    for (; m.find(); end = m.end()) {
      if (m.start() > end) {
        parts.add(todo(raw, end, m.start()));
      }
      String name = m.group(1);
      String mySrc = m.group(2);
      Check.on(emptyTemplateName(src, offset + m.start(1)), name).is(notBlank());
      Check.on(duplicateTemplateName(src, offset + m.start(1), name), name).is(notIn(), names);
      names.add(name);
      Template t = Template.parse(name, clazz, mySrc);
      parts.add(new NestedTemplatePart(t, offset + m.start(), offset + m.end()));
    }
    if (end < raw.text().length()) {
      parts.add(todo(raw, end, raw.text().length()));
    }
    return parts;
  }

  private List<Part> parseIncludes(UnparsedPart raw, Set<String> names) throws ParseException {
    List<Part> parts = new ArrayList<>();
    Matcher m = REGEX_INCLUDE.matcher(raw.text());
    int offset = raw.start(), end = 0;
    for (; m.find(); end = m.end()) {
      if (m.start() > end) {
        parts.add(todo(raw, end, m.start()));
      }
      String name = m.group(2);
      String path = m.group(3);
      Check.on(emptyPath(src, offset + m.start(3)), path).is(notBlank());
      if (name == null) {
        name = IncludedTemplatePart.basename(path);
      }
      Check.on(emptyTemplateName(src, offset + m.start(2)), name).is(notBlank());
      Check.on(duplicateTemplateName(src, offset + m.start(2), name), name).is(notIn(), names);
      Check.on(missingClassObject(src, offset + m.start(3), name, path), clazz).is(notNull());
      names.add(name);
      Template t = Template.parse(name, clazz, Path.of(path));
      parts.add(new IncludedTemplatePart(t, offset + m.start(), offset + m.end()));
    }
    if (end < raw.text().length()) {
      parts.add(todo(raw, end, raw.text().length()));
    }
    return parts;
  }

  private List<Part> parseVariables(UnparsedPart raw, Set<String> names) throws ParseException {
    List<Part> parts = new ArrayList<>();
    Matcher m = REGEX_VARIABLE.matcher(raw.text());
    int offset = raw.start(), end = 0;
    for (; m.find(); end = m.end()) {
      if (m.start() > end) {
        parts.add(todo(raw, end, m.start()));
      }
      String esc = m.group(2);
      String name = m.group(3);
      EscapeType escType =
          Check.catching(badEscapeType(src, offset + m.start(2), esc), esc, EscapeType::parse).ok();
      Check.on(emptyVarName(src, offset + m.start(3)), name).is(notBlank());
      Check.on(duplicateVarName(src, offset + m.start(3), name), name).is(notIn(), names);
      parts.add(new VariablePart(escType, name, offset + m.start(), offset + m.end()));
    }
    if (end < raw.text().length()) {
      parts.add(todo(raw, end, raw.text().length()));
    }
    return parts;
  }

  private void checkGarbage(UnparsedPart unparsed) throws ParseException {
    String str = unparsed.text();
    int off = unparsed.start();
    Matcher m1 = REGEX_TMPL_END.matcher(str);
    Check.on(s -> danglingEndOfTemplate(src, off + m1.start()), m1.find()).is(no());
    int idx = str.indexOf(TMPL_START + "begin:");
    Check.on(templateNotTerminated(src, off + idx), idx).is(eq(), -1);
    idx = str.indexOf(TMPL_START + "end:");
    Check.on(templateNotTerminated(src, off + idx), idx).is(eq(), -1);
    idx = str.indexOf(TMPL_START + "include:");
    Check.on(includeNotTerminated(src, off + idx), idx).is(eq(), -1);
  }

  private static List<Part> uncomment(List<Part> in, Pattern commentPattern) {
    List<Part> out = new ArrayList<>(in.size());
    for (int i = 0; i < in.size(); ++i) {
      UnparsedPart unparsed = (UnparsedPart) in.get(i);
      Matcher m = commentPattern.matcher(unparsed.text());
      int end = 0;
      for (; m.find(); end = m.end()) {
        if (m.start() > end) {
          // Create unparsed part for everything up to <!--
          out.add(todo(unparsed, end, m.start()));
        }
        // Create unparsed part for variable/template. Parsing is done later.
        out.add(todo(unparsed, m.start(1), m.end(1)));
        // Jump past --> with next m.end()
      }
      if (end < unparsed.text().length()) {
        // Create unparsed part for everything after last -->
        out.add(todo(unparsed, end, unparsed.text().length()));
      }
    }
    return out;
  }

  private static List<Part> purgeDitchBlocks(String src) throws ParseException {
    Matcher m = REGEX_DITCH_TOKEN.matcher(src);
    if (!m.find()) {
      return Collections.singletonList(new UnparsedPart(src, 0, src.length()));
    }
    List<Part> parts = new ArrayList<>();
    int end = 0;
    do {
      // Create unparsed part for everything up to <!--%%-->
      if (m.start() > end) {
        parts.add(new UnparsedPart(src.substring(end, m.start()), end, m.start()));
      }
      // Find the next occurrence of <!--%%--> and jump all the way past it, ignoring everything in
      // between
      end =
          Check.on(ditchBlockNotTerminated(src, m.start()), m)
              .has(Matcher::find, yes())
              .ok(Matcher::end);
    } while (m.find());
    if (end < src.length()) {
      // Create unparsed part for everything after last <!--%%-->
      parts.add(new UnparsedPart(src.substring(end), end, src.length()));
    }
    return parts;
  }

  private static UnparsedPart todo(UnparsedPart p, int from, int to) {
    String s = p.text().substring(from, to);
    return new UnparsedPart(s, from + p.start(), to + p.start());
  }
}
