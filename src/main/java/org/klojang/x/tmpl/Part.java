package org.klojang.x.tmpl;

import org.klojang.template.Template;
import org.klojang.template.TemplateUtils;

/**
 * A {@code Part} represents a well-defined part within a template file. There are parts containing
 * {@link VariablePart variable declarations}, {@link InlineTemplatePart inline templates}
 * (everything between a pair of {@code ~%%begin} and {@code ~%%end} tags), {@link
 * IncludedTemplatePart included templates} and for {@link TextPart boilerplate text}. You can see
 * how the template parser has broken up your template using {@link
 * TemplateUtils#printParts(Template, java.io.PrintStream)}.
 *
 * <p>Note that the two comments-like substructures within a template file (ditch blocks and
 * placeholders) are not represented by a concrete {@code Part} implementation. They disappear
 * without leaving a trace in the {@link Template} instance ultimately produced by the parser. Ditch
 * blocks are removed in a pre-processing phase; placeholders in a post-processing phase.
 *
 * @author Ayco Holleman
 */
public interface Part {

  /**
   * The start index of this part within the template.
   *
   * @return The start index of this part within the template.
   */
  int start();

  Template getParentTemplate();
}
