package org.klojang.template;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.check.Check;
import nl.naturalis.common.function.ThrowingBiFunction;
import static org.klojang.template.ParseException.*;
import static org.klojang.template.Template.ROOT_TEMPLATE_NAME;
import static org.klojang.template.TemplateSourceType.STRING;
import static nl.naturalis.common.StringMethods.EMPTY;
import static nl.naturalis.common.check.CommonChecks.blank;
import static nl.naturalis.common.check.CommonChecks.eq;
import static nl.naturalis.common.check.CommonChecks.equalTo;
import static nl.naturalis.common.check.CommonChecks.in;

class Parser {

  private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

  private static interface PartialParser
      extends ThrowingBiFunction<UnparsedPart, Set<String>, List<Part>, ParseException> {}

  private final String tmplName;
  private final TemplateId id;
  private final String src;

  Parser(String tmplName, TemplateId id) throws InvalidPathException {
    this(tmplName, id, id.getSource());
  }

  Parser(String tmplName, TemplateId id, String src) {
    this.tmplName = tmplName;
    this.id = id;
    this.src = src;
  }

  Template parse() throws ParseException {
    logParsing(tmplName, id);
    // Accumulates template names for duplicate checks:
    Set<String> namesInUse = new HashSet<>();
    List<Part> parts = List.of(new UnparsedPart(src, 0));
    parts = purgeDitchBlocks(parts);
    parts = parse(parts, namesInUse, (x, y) -> parseInlineTmpls(x, y, true));
    parts = parse(parts, namesInUse, (x, y) -> parseInlineTmpls(x, y, false));
    parts = parse(parts, namesInUse, (x, y) -> parseIncludedTmpls(x, y, true));
    parts = parse(parts, namesInUse, (x, y) -> parseIncludedTmpls(x, y, false));
    parts = parse(parts, namesInUse, (x, y) -> parseVars(x, y, true));
    parts = parse(parts, namesInUse, (x, y) -> parseVars(x, y, false));
    parts = collectTextParts(parts);
    return new Template(tmplName, id, List.copyOf(parts));
  }

  private static List<Part> parse(List<Part> in, Set<String> names, PartialParser parser)
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

  private static List<Part> purgeDitchBlocks(List<Part> parts) throws ParseException {
    LOG.trace("--> Purging ditch blocks");
    List<Part> out = new ArrayList<>();
    for (Part p : parts) {
      if (p instanceof UnparsedPart) {
        out.addAll(purgeDitchBlocksInPart((UnparsedPart) p));
      } else {
        out.add(p);
      }
    }
    return out;
  }

  private static List<Part> purgeDitchBlocksInPart(UnparsedPart unparsed) throws ParseException {
    String src = unparsed.text();
    Matcher m = Regex.of().ditchBlock.matcher(src);
    if (!m.find()) {
      return Collections.singletonList(unparsed);
    }
    List<Part> parts = new ArrayList<>();
    int end = 0;
    do {
      int start = m.start();
      if (start > end) {
        parts.add(todo(unparsed, end, m.start()));
      }
      end = m.end();
    } while (m.find());
    if (end < src.length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
    }
    return parts;
  }

  private List<Part> parseInlineTmpls(UnparsedPart unparsed, Set<String> names, boolean hidden)
      throws ParseException {
    LOG.trace("--> Parsing inline templates");
    Pattern p = hidden ? Regex.of().cmtInlineTemplate : Regex.of().inlineTemplate;
    Matcher m = match(p, unparsed);
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
      Parser parser = new Parser(name, new TemplateId(id), mySrc);
      parts.add(new InlineTemplatePart(parser.parse(), offset + m.start()));
      end = m.end();
    } while (m.find());
    if (end < unparsed.text().length()) {
      parts.add(todo(unparsed, end, unparsed.text().length()));
    }
    return parts;
  }

  private List<Part> parseIncludedTmpls(UnparsedPart unparsed, Set<String> names, boolean hidden)
      throws ParseException {
    Pattern p = hidden ? Regex.of().cmtIncludedTemplate : Regex.of().includedTemplate;
    Matcher m = match(p, unparsed);
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

  private List<Part> parseVars(UnparsedPart unparsed, Set<String> names, boolean hidden)
      throws ParseException {
    Pattern p = hidden ? Regex.of().cmtVariable : Regex.of().variable;
    Matcher m = match(p, unparsed);
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

  /* Text parts are all unparsed parts that remain after everything else has been parsed out */
  private List<Part> collectTextParts(List<Part> in) throws ParseException {
    List<Part> out = new ArrayList<>(in.size());
    for (Part p : in) {
      if (p.getClass() == UnparsedPart.class) {
        UnparsedPart unparsed = (UnparsedPart) p;
        if (unparsed.text().length() != 0) {
          checkGarbage(unparsed);
          String text = Regex.of().placeholder.matcher(unparsed.text()).replaceAll(EMPTY);
          if (text.contains(Regex.PLACEHOLDER_TAG)) {
            int idx = p.start() + unparsed.text().lastIndexOf(Regex.PLACEHOLDER_TAG);
            throw placeholderNotTerminated(src, idx);
          }
          out.add(new TextPart(text, p.start()));
        }
      } else {
        out.add(p);
      }
    }
    return out;
  }

  private void checkGarbage(UnparsedPart unparsed) throws ParseException {
    String str = unparsed.text();
    int off = unparsed.start();
    int idx0 = str.indexOf(Regex.TMPL_START + "begin:");
    Check.on(beginTagNotTerminated(src, off + idx0), idx0).is(eq(), -1);
    int idx1 = str.indexOf(Regex.TMPL_START + "end:");
    Check.on(endTagNotTerminated(src, off + idx1), idx1).is(eq(), -1);
    int idx2 = str.indexOf(Regex.TMPL_START + "include:");
    Check.on(includeTagNotTerminated(src, off + idx2), idx2).is(eq(), -1);
    Matcher m = Regex.of().beginTag.matcher(str);
    if (m.find()) {
      throw missingEndTag(str, off + m.start(), m.group(1));
    }
    m = Regex.of().endTag.matcher(str);
    if (m.find()) {
      throw danglingEndTag(str, off + m.start(), m.group(1));
    }
    m = Regex.of().ditchTag.matcher(str);
    if (m.find()) {
      throw ditchBlockNotTerminated(src, off + m.start());
    }
  }

  private static Matcher match(Pattern pattern, UnparsedPart unparsed) {
    return pattern.matcher(unparsed.text());
  }

  private static UnparsedPart todo(UnparsedPart p, int from, int to) {
    String s = p.text().substring(from, to);
    return new UnparsedPart(s, from + p.start());
  }

  private static void logParsing(String name, TemplateId id) {
    if (LOG.isTraceEnabled()) {
      if (name == ROOT_TEMPLATE_NAME) {
        LOG.trace("Parsing template {}", name);
      } else if (id.sourceType() == STRING) {
        LOG.trace("Parsing inline template \"{}\"", name);
      } else {
        LOG.trace("Parsing included template \"{}\"", name);
      }
    }
  }
}
