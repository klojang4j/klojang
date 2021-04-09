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
import nl.naturalis.yokete.render.EscapeType;
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
    parts = uncomment(parts, REGEX_NESTED_CMT);
    parts = uncomment(parts, REGEX_INCLUDE_CMT);
    parts = uncomment(parts, REGEX_VARIABLE_CMT);
    // Accumulates template names for duplicate checks:
    Set<String> namesInUse = new HashSet<>();
    parts = parse(parts, namesInUse, this::parseInlineTmpls);
    parts = parse(parts, namesInUse, this::parseIncludedTmpls);
    parts = parse(parts, namesInUse, this::parseVars);
    parts = collectTextParts(parts);
    return new Template(tmplName, path, List.copyOf(parts));
  }

  /*
   * Ditch blocks consist of a pair of ditch tokens (<!--%%-->) and any text inside of
   * the tokens. They are discarded without leaving a trace in the Template instance
   * created by the Parser.
   */
  private static List<Part> purgeDitchBlocks(String src) throws ParseException {
    Matcher m = REGEX_DITCH_TOKEN.matcher(src);
    if (!m.find()) {
      return Collections.singletonList(new UnparsedPart(src, 0));
    }
    List<Part> parts = new ArrayList<>();
    int end = 0;
    do {
      int start = m.start();
      if (start > end) {
        parts.add(new UnparsedPart(src.substring(end, start), end));
      }
      if (!m.find()) {
        throw ditchBlockNotTerminated(src, start);
      }
      end = m.end();
    } while (m.find());
    if (end < src.length()) {
      parts.add(new UnparsedPart(src.substring(end), end));
    }
    return parts;
  }

  /*
   * Turns "<!-- ~%firstName% -->" into an UnparsedPart containing "~%firstName%", which
   * will then be picked up for further processing by parseVars().
   */
  private static List<Part> uncomment(List<Part> in, Pattern cmtPattern) {
    List<Part> out = new ArrayList<>(in.size());
    for (Part p : in) {
      UnparsedPart unparsed = (UnparsedPart) p;
      int end = 0;
      for (Matcher m = match(cmtPattern, unparsed); m.find(); end = m.end()) {
        if (m.start() > end) {
          out.add(todo(unparsed, end, m.start()));
        }
        out.add(todo(unparsed, m.start(1), m.end(1)));
      }
      if (end < unparsed.text().length()) {
        out.add(todo(unparsed, end, unparsed.text().length()));
      }
    }
    return out;
  }

  @SuppressWarnings("static-method")
  private List<Part> parse(List<Part> in, Set<String> names, PartialParser parser)
      throws ParseException {
    List<Part> out = new ArrayList<>(in.size() + 10);
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
    List<Part> out = new ArrayList<>(in.size());
    for (Part p : in) {
      if (p.getClass() == UnparsedPart.class) {
        UnparsedPart unparsed = (UnparsedPart) p;
        if (unparsed.text().strip().length() != 0) {
          checkGarbage(unparsed);
          out.add(unparsed.toTextPart());
        }
      } else {
        out.add(p);
      }
    }
    return out;
  }

  private List<Part> parseInlineTmpls(UnparsedPart unparsed, Set<String> names)
      throws ParseException {
    List<Part> parts = new ArrayList<>();
    int offset = unparsed.start(), end = 0;
    for (Matcher m = match(REGEX_NESTED, unparsed); m.find(); end = m.end()) {
      if (m.start() > end) {
        parts.add(todo(unparsed, end, m.start()));
      }
      String name = m.group(1);
      String mySrc = m.group(2);
      Check.on(emptyTemplateName(src, offset + m.start(1)), name).isNot(blank());
      Check.on(duplicateTemplateName(src, offset + m.start(1), name), name)
          .isNot(in(), names)
          .isNot(equalTo(), Template.ROOT_TEMPLATE_NAME);
      names.add(name);
      Parser parser = new Parser(name, clazz, mySrc);
      Template nested = parser.parse();
      parts.add(new InlineTemplatePart(nested, offset + m.start()));
    }
    if (end < unparsed.text().length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
    }
    return parts;
  }

  private List<Part> parseIncludedTmpls(UnparsedPart unparsed, Set<String> names)
      throws ParseException {
    List<Part> parts = new ArrayList<>();
    int offset = unparsed.start(), end = 0;
    for (Matcher m = match(REGEX_INCLUDE, unparsed); m.find(); end = m.end()) {
      if (m.start() > end) {
        parts.add(todo(unparsed, end, m.start()));
      }
      String name = m.group(2);
      String path = m.group(3);
      Check.on(emptyPath(src, offset + m.start(3)), path).isNot(blank());
      if (name == null) {
        name = IncludedTemplatePart.basename(path);
      }
      Check.on(emptyTemplateName(src, offset + m.start(2)), name).isNot(blank());
      Check.on(duplicateTemplateName(src, offset + m.start(2), name), name)
          .isNot(in(), names)
          .isNot(equalTo(), Template.ROOT_TEMPLATE_NAME);
      Check.on(missingClassObject(src, offset + m.start(3), name, path), clazz).is(notNull());
      Check.on(invalidIncludePath(src, offset + m.start(3), path), clazz.getResource(path))
          .is(notNull());
      names.add(name);
      Parser parser = new Parser(name, clazz, Path.of(path));
      Template nested = parser.parse();
      parts.add(new IncludedTemplatePart(nested, offset + m.start()));
    }
    if (end < unparsed.text().length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
    }
    return parts;
  }

  private List<Part> parseVars(UnparsedPart unparsed, Set<String> names) throws ParseException {
    List<Part> parts = new ArrayList<>();
    int offset = unparsed.start(), end = 0;
    for (Matcher m = match(REGEX_VARIABLE, unparsed); m.find(); end = m.end()) {
      if (m.start() > end) {
        parts.add(todo(unparsed, end, m.start()));
      }
      EscapeType escType;
      try {
        escType = EscapeType.parse(m.group(2));
      } catch (IllegalArgumentException e) {
        throw badEscapeType(src, offset + m.start(2), m.group(2));
      }
      String name = m.group(3);
      Check.on(emptyVarName(src, offset + m.start(3)), name).isNot(blank());
      Check.on(duplicateVarName(src, offset + m.start(3), name), name).isNot(in(), names);
      parts.add(new VariablePart(escType, name, offset + m.start()));
    }
    if (end < unparsed.text().length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
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

  private static Matcher match(Pattern pattern, UnparsedPart unparsed) {
    return pattern.matcher(unparsed.text());
  }

  private static UnparsedPart todo(UnparsedPart p, int from, int to) {
    String s = p.text().substring(from, to);
    return new UnparsedPart(s, from + p.start());
  }
}
