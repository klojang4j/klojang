package nl.naturalis.yokete.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static nl.naturalis.common.check.CommonChecks.gte;
import static nl.naturalis.common.check.CommonChecks.ne;
import static nl.naturalis.common.check.CommonChecks.negative;
import static nl.naturalis.common.check.CommonChecks.positive;
import static nl.naturalis.yokete.helpers.PagingHelper.Type.SHIFT_WINDOW;

public class PagingHelper {

  private static final String ERR_NO_ROW_COUNT = "Row count not set";

  public static enum Type {
    SHIFT_WINDOW,
    NEXT_WINDOW;
  }

  private final Type type;
  private final int numPageLinks;

  // Settable
  private int rowsPerPage = 10;
  private int rowCount = -1;

  // Maintained internally
  private int page = 0; // selected page number
  private int first = 0; // first of the numPageLinks page numbers

  public PagingHelper() {
    this(SHIFT_WINDOW, 10);
  }

  public PagingHelper(int numPageLinks) {
    this(SHIFT_WINDOW, numPageLinks);
  }

  public PagingHelper(Type type, int numPageLinks) {
    this.type = Check.notNull(type, "type").ok();
    this.numPageLinks = Check.that(numPageLinks, "numPageLinks").is(gte(), 2).intValue();
  }

  public PagingHelper rowCount(int rowCount) {
    this.rowCount = Check.that(rowCount).isNot(negative()).intValue();
    return this;
  }

  public PagingHelper rowsPerPage(int rowsPerPage) {
    this.rowsPerPage = Check.that(rowsPerPage).is(positive()).intValue();
    return this;
  }

  public PagingHelper firstPage() {
    page = 0;
    first = 0;
    return this;
  }

  public PagingHelper prevPage() {
    page = Math.max(0, --page);
    if (page < first) {
      first = shift() ? page : nextWindow();
    }
    return this;
  }

  public PagingHelper nextPage() {
    page = clamp(++page);
    if (page >= first + numPageLinks) {
      if (shift()) {
        first = Math.max(0, page - numPageLinks + 1);
      } else {
        first = nextWindow();
      }
    }
    return this;
  }

  public PagingHelper lastPage() {
    Check.that(rowCount).is(ne(), -1, ERR_NO_ROW_COUNT);
    page = clamp(getPageCount());
    if (shift()) {
      first = Math.max(0, page - numPageLinks + 1);
    } else {
      first = nextWindow();
    }
    return this;
  }

  public PagingHelper page(int pageNo) {
    Check.that(rowCount).is(ne(), -1, ERR_NO_ROW_COUNT);
    page = clamp(pageNo);
    if (page < first) {
      // That's odd; the user could not have requested this page by clicking
      // one of the page links so has probably been tinkering with the URL,
      // which is fine by us
      first = shift() ? page : nextWindow();
    } else if (page >= first + numPageLinks) {
      if (shift()) {
        first = Math.max(0, page - numPageLinks + 1);
      } else {
        first = nextWindow();
      }
    }
    return this;
  }

  public List<Tuple<Boolean, Integer>> data() {
    Check.that(rowCount).is(ne(), -1, ERR_NO_ROW_COUNT);
    if (rowCount <= rowsPerPage) {
      return Collections.emptyList();
    }
    int numLinks = Math.min(getPageCount() - first, numPageLinks);
    List<Tuple<Boolean, Integer>> tuples = new ArrayList<>(numLinks);
    for (int i = first; i < first + numLinks; ++i) {
      tuples.add(Tuple.of(i == page, i + 1));
    }
    return tuples;
  }

  private int nextWindow() {
    return clamp(page - (page % numPageLinks));
  }

  private int clamp(int pageNo) {
    return Math.min(getPageCount() - 1, Math.max(0, pageNo));
  }

  private boolean shift() {
    return type == SHIFT_WINDOW;
  }

  private int getPageCount() {
    return (int) Math.ceil((double) rowCount / (double) rowsPerPage);
  }
}
