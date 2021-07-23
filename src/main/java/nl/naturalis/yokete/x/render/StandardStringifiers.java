package nl.naturalis.yokete.x.render;

import java.util.Map;
import java.util.function.UnaryOperator;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.http.client.utils.URIBuilder;
import nl.naturalis.yokete.render.Stringifier;
import static org.apache.commons.text.StringEscapeUtils.escapeEcmaScript;
import static nl.naturalis.common.StringMethods.EMPTY;

public class StandardStringifiers {

  private StandardStringifiers() {}

  // Copied from StringEscapeUtils and added the 4th LookupTranslator
  private static final CharSequenceTranslator HTML_ATTR_TRANSLATOR =
      new AggregateTranslator(
          new LookupTranslator(EntityArrays.BASIC_ESCAPE),
          new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE),
          new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE),
          new LookupTranslator(Map.of("'", "&#39;", "\"", "&#34;")));

  public static Stringifier ESCAPE_HTML = wrap(StringEscapeUtils::escapeHtml4);

  public static Stringifier ESCAPE_JS = wrap(StringEscapeUtils::escapeEcmaScript);

  public static Stringifier ESCAPE_ATTR = wrap(HTML_ATTR_TRANSLATOR::translate);

  public static Stringifier ESCAPE_JS_ATTR =
      x -> {
        if (x == null) {
          return EMPTY;
        }
        return HTML_ATTR_TRANSLATOR.translate(escapeEcmaScript(x.toString()));
      };

  public static Stringifier URL_QUERY_PARAM =
      wrap(x -> new URIBuilder().setPathSegments(x).toString().substring(1));

  public static Stringifier URL_PATH_SEGMENT =
      wrap(x -> new URIBuilder().addParameter("x", x).toString().substring(3));

  private static Stringifier wrap(UnaryOperator<String> stringifier) {
    return x -> x == null ? EMPTY : stringifier.apply(x.toString());
  }
}
