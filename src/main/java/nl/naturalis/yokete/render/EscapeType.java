package nl.naturalis.yokete.render;

import java.util.function.UnaryOperator;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.utils.URIBuilder;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.StringMethods.isEmpty;

/**
 * Symbolic constants for various text-escaping methods.
 *
 * @author Ayco Holleman
 */
public enum EscapeType {

  /**
   * The escape type assigned to template variables that don't specify an inline escape type. For
   * example, the escape type of {@code ~%html:person.address.street%} is {@code ESCAPE_HTML}
   * whereas the escape type of {@code ~%person.address.street%} is {@code NOT_SPECIFIED}. It is not
   * allowed to pass this {@code EscapeType} any of the methods in the {@link Renderer} class.
   * However, if a template variable's escape type is {@code NOT_SPECIFIED}, its value will be
   * escaped using whatever {@code EscapeTye} <i>is</i> passed to these methods.
   */
  NOT_SPECIFIED(
      null,
      s -> {
        throw new UnsupportedOperationException();
      }),

  /**
   * Do not apply any escaping. This is (most likely) the {@code EscapeType} to use if a template
   * variable stands in for an entire block of HTML. Anything that was variable in there has has
   * likely already been substituted and escaped somewhere upstream. Further escaping would destroy
   * the HTML tags themselves. This escape type can be specified within the template variable itself
   * using the {@code text} prefix. For example: {@code ~%text:tableRows%}
   */
  ESCAPE_NONE("text", UnaryOperator.identity()),
  /**
   * The type of escaping to be used for template variables inserted into HTML tags. For example:
   *
   * <p>
   *
   * <pre>
   * &lt;tr&gt;&lt;td&gt;~%fullName%&lt;/td&gt;&lt;/tr&gt;
   * </pre>
   *
   * <p>This escape type can be specified within the template variable itself using the {@code html}
   * prefix:
   *
   * <p>
   *
   * <pre>
   * &lt;tr&gt;&lt;td&gt;~%html:fullName%&lt;/td&gt;&lt;/tr&gt;
   * </pre>
   */
  ESCAPE_HTML("html", StringEscapeUtils::escapeHtml4),
  /**
   * The type of escaping to be used for template variables inserted into Javascript. For example:
   *
   * <p>
   *
   * <pre>
   * &lt;script&gt;
   *  var fullName = "~%fullName%";
   * &lt;/script&gt;
   * </pre>
   *
   * <p>This escape type can be specified within the template variable itself using the {@code js}
   * prefix:
   *
   * <p>
   *
   * <pre>
   * &lt;script&gt;
   *  var fullName = '~%js:fullName%';
   * &lt;/script&gt;
   * </pre>
   */
  ESCAPE_JS("js", StringEscapeUtils::escapeEcmaScript),

  /**
   * The type of escaping to use if a template variable is embedded as a path segment within a URL.
   * Note that it does not matter whether the URL as a whole is the value of a JavaScript variable
   * or the contents of an HTML tag. The result of applying this escape type does not change when
   * subsequently applying JavaScript or HTML escaping.
   */
  ESCAPE_URL_PATH_SEGMENT("ps", EscapeType::escapeUrlPathSegment),

  /**
   * The type of escaping to use if a template variable is embedded within a URL as a the value of
   * URL query parameter. It can also be used in the more unlikely case that the variable functions
   * as the <i>name</i> of the query parameter, because names and values are escaped in the same way
   * in a URL. Note that it does not matter whether the URL as a whole is the value of a JavaScript
   * variable or the contents of an HTML tag. The result of applying this escape type does not
   * change when subsequently applying JavaScript or HTML escaping.
   */
  ESCAPE_URL_QUERY_PARAM("qp", EscapeType::escapeUrlQueryParam);

  /**
   * Parses the specified string into an {@code EscapeType}. Valid values are "text", "html" and
   * "js", corresponding to the available variable type prefixes (e.g. ~%html:fullName%). The string
   * is also allowed to be null or empty, in which case this method returns null. This corresponds
   * to a variable without a type prefix (~%fullName%).
   *
   * @param s The string to parse
   * @return The {@code EscapeType} corresponding to the string
   */
  public static EscapeType parse(String s) {
    if (isEmpty(s)) {
      return NOT_SPECIFIED;
    }
    switch (s) {
      case "text":
        return ESCAPE_NONE;
      case "html":
        return ESCAPE_HTML;
      case "js":
        return ESCAPE_JS;
      case "ps":
        return ESCAPE_URL_PATH_SEGMENT;
      case "qp":
        return ESCAPE_URL_QUERY_PARAM;
    }
    return Check.fail("No such escape type: \"%s\"", s);
  }

  private final String prefix;
  private final UnaryOperator<String> escaper;

  private EscapeType(String prefix, UnaryOperator<String> escaper) {
    this.prefix = prefix;
    this.escaper = escaper;
  }

  /**
   * Returns the equivalent prefix for template variables.
   *
   * @return The equivalent prefix for template variables
   */
  public String getPrefix() {
    return prefix;
  }
  /**
   * Escapes the specified string according to this {@code EscapeType}.
   *
   * @param raw The input string
   * @return The string escaped according to this {@code EscapeType}
   */
  public String apply(String raw) {
    return escaper.apply(raw);
  }

  private static String escapeUrlPathSegment(String s) {
    // Chop off the forward slash
    return new URIBuilder().setPathSegments(s).toString().substring(1);
  }

  private static String escapeUrlQueryParam(String s) {
    // Chop off ?x=
    return new URIBuilder().addParameter("x", s).toString().substring(3);
  }
}
