package org.klojang.template;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.klojang.x.tmpl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.naturalis.common.function.ThrowingBiFunction;
import static org.klojang.template.ErrorType.*;
import static org.klojang.template.Template.ROOT_TEMPLATE_NAME;
import static org.klojang.x.tmpl.TemplateSourceType.STRING;
import static nl.naturalis.common.StringMethods.EMPTY;
import static nl.naturalis.check.CommonChecks.blank;
import static nl.naturalis.check.CommonChecks.eq;
import static nl.naturalis.check.CommonChecks.EQ;
import static nl.naturalis.check.CommonChecks.in;

class Parser {

  private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

  private static interface PartialParser
      extends ThrowingBiFunction<UnparsedPart, Set<String>, List<Part>, ParseException> {}

  private final String tmplName;
  private final TemplateId id;
  private final String src;

  Parser(String tmplName, TemplateId id) throws PathResolutionException {
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
      EMPTY_TMPL_NAME.check(name, src, offset + m.start(1)).isNot(blank());
      DUPLICATE_TMPL_NAME
          .check(name, src, offset + m.start(1), name)
          .isNot(in(), names)
          .isNot(EQ(), ROOT_TEMPLATE_NAME);
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
      EMPTY_INCLUDE_PATH.check(path, src, offset + m.start(3)).isNot(blank());
      if (name == null) {
        name = IncludedTemplatePart.basename(path);
      }
      EMPTY_TMPL_NAME.check(name, src, offset + m.start(2)).isNot(blank());
      DUPLICATE_TMPL_NAME
          .check(name, src, offset + m.start(2), name)
          .isNot(in(), names)
          .isNot(EQ(), ROOT_TEMPLATE_NAME);
      TemplateId newId;
      if (id.clazz() != null) { // Load as resource
        if (id.clazz().getResource(path) == null) {
          throw INVALID_INCLUDE_PATH.asException(src, offset + m.start(3), path);
        }
        newId = new TemplateId(id.clazz(), path);
      } else if (id.pathResolver() != null) { // Load using path resolver
        PathResolver pr = id.pathResolver();
        if (!pr.isValidPath(path).isEmpty() && !pr.isValidPath(path).get()) {
          throw INVALID_INCLUDE_PATH.asException(src, offset + m.start(3), path);
        }
        newId = new TemplateId(id.pathResolver(), path);
      } else { // Load from file system
        if (!new File(path).isFile()) {
          throw INVALID_INCLUDE_PATH.asException(src, offset + m.start(3), path);
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
      EMPTY_VAR_NAME.check(name, src, offset + m.start(3)).isNot(blank());
      VAR_NAME_WITH_TMPL_NAME.check(name, src, offset + m.start(3), name).isNot(in(), names);
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
            int idx = p.start() + unparsed.text().indexOf(Regex.PLACEHOLDER_TAG);
            throw PLACEHOLDER_NOT_CLOSED.asException(text, idx);
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
    BEGIN_TAG_NOT_TERMINATED.checkInt(idx0, src, off + idx0).is(eq(), -1);
    int idx1 = str.indexOf(Regex.TMPL_START + "end:");
    END_TAG_NOT_TERMINATED.checkInt(idx1, src, off + idx1).is(eq(), -1);
    int idx2 = str.indexOf(Regex.TMPL_START + "include:");
    INCLUDE_TAG_NOT_TERMINATED.checkInt(idx2, src, off + idx2).is(eq(), -1);
    Matcher m = Regex.of().beginTag.matcher(str);
    if (m.find()) {
      throw MISSING_END_TAG.asException(src, off + m.start(), m.group(1));
    }
    m = Regex.of().endTag.matcher(str);
    if (m.find()) {
      throw DANGLING_END_TAG.asException(src, off + m.start(), m.group(1));
    }
    m = Regex.of().ditchTag.matcher(str);
    if (m.find()) {
      throw DITCH_BLOCK_NOT_CLOSED.asException(src, off + m.start());
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
        LOG.trace("Parsing root template");
      } else if (id.sourceType() == STRING) {
        LOG.trace("Parsing inline template \"{}\"", name);
      } else {
        LOG.trace("Parsing included template \"{}\"", name);
      }
    }
  }
}
