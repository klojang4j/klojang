package org.klojang.template;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.function.ThrowingBiFunction;
import static org.klojang.template.ParseException.*;
import static org.klojang.template.Regex.*;
import static nl.naturalis.common.check.CommonChecks.*;

class Parser {

  private static interface PartialParser
      extends ThrowingBiFunction<UnparsedPart, Set<String>, List<Part>, ParseException> {}

  private final String tmplName;
  private final TemplateId id;
  private final String src;

  Parser(String tmplName, TemplateId id, String src) {
    this.tmplName = tmplName;
    this.id = id;
    this.src = src;
  }

  Parser(String tmplName, TemplateId id) throws InvalidPathException {
    this.tmplName = tmplName;
    this.id = id;
    this.src = id.getSource();
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
    return new Template(tmplName, id, List.copyOf(parts));
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
        if (unparsed.text().length() != 0) {
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
    Matcher m = match(REGEX_NESTED, unparsed);
    if (!m.find()) {
      return Collections.singletonList(unparsed);
    }
    List<Part> parts = new ArrayList<>();
    int offset = unparsed.start(), end = 0;
    do {
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
      Parser parser = new Parser(name, id, mySrc);
      parts.add(new InlineTemplatePart(parser.parse(), offset + m.start()));
      end = m.end();
    } while (m.find());
    if (end < unparsed.text().length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
    }
    return parts;
  }

  private List<Part> parseIncludedTmpls(UnparsedPart unparsed, Set<String> names)
      throws ParseException {
    Matcher m = match(REGEX_INCLUDE, unparsed);
    if (!m.find()) {
      return Collections.singletonList(unparsed);
    }
    List<Part> parts = new ArrayList<>();
    int offset = unparsed.start(), end = 0;
    do {
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
      TemplateId newId;
      if (id.clazz() != null) { // Load as resource
        if (id.clazz().getResource(path) == null) {
          throw invalidIncludePath(src, offset + m.start(3), path);
        }
        newId = new TemplateId(id.clazz(), path);
      } else if (id.pathResolver() != null) { // Load using path resolver
        PathResolver pr = id.pathResolver();
        if (!pr.isValidPath(path).isEmpty() && !pr.isValidPath(path).get()) {
          throw invalidIncludePath(src, offset + m.start(3), path);
        }
        newId = new TemplateId(id.pathResolver(), path);
      } else { // Load from file system
        if (!new File(path).isFile()) {
          throw invalidIncludePath(src, offset + m.start(3), path);
        }
        newId = new TemplateId(path);
      }
      names.add(name);
      Template nested = TemplateCache.INSTANCE.get(name, newId);
      parts.add(new IncludedTemplatePart(nested, offset + m.start()));
      end = m.end();
    } while (m.find());
    if (end < unparsed.text().length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
    }
    return parts;
  }

  private List<Part> parseVars(UnparsedPart unparsed, Set<String> names) throws ParseException {
    Matcher m = match(REGEX_VARIABLE, unparsed);
    if (!m.find()) {
      return Collections.singletonList(unparsed);
    }
    List<Part> parts = new ArrayList<>();
    int offset = unparsed.start(), end = 0;
    do {
      if (m.start() > end) {
        parts.add(todo(unparsed, end, m.start()));
      }
      String prefix = m.group(2);
      String name = m.group(3);
      Check.on(emptyVarName(src, offset + m.start(3)), name).isNot(blank());
      Check.on(duplicateVarName(src, offset + m.start(3), name), name).isNot(in(), names);
      parts.add(new VariablePart(prefix, name, offset + m.start()));
      end = m.end();
    } while (m.find());
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