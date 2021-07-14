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
import static nl.naturalis.yokete.helpers.PagingDataHelper.ScrollType.SHIFT_WINDOW;

public class PagingDataHelper {

  private static final String ERR_NO_ROW_COUNT = "Row count not set";

  public static enum ScrollType {
    SHIFT_WINDOW,
    NEXT_WINDOW;
  }

  private final ScrollType scrollType;
  private final int windowSize;

  // Settable
  private int rowsPerPage = 10;
  private int rowCount = -1;

  // Maintained internally
  private int page = 0; // selected page number
  private int first = 0; // first of the page numbers in window

  public PagingDataHelper() {
    this(SHIFT_WINDOW, 10);
  }

  public PagingDataHelper(int windowSize) {
    this(SHIFT_WINDOW, windowSize);
  }

  public PagingDataHelper(ScrollType scrollType, int windowSize) {
    this.scrollType = Check.notNull(scrollType, "scrollType").ok();
    this.windowSize = Check.that(windowSize, "windowSize").is(gte(), 2).intValue();
  }

  public PagingDataHelper setRowCount(int rowCount) {
    this.rowCount = Check.that(rowCount).isNot(negative()).intValue();
    return this;
  }

  public PagingDataHelper setRowsPerPage(int rowsPerPage) {
    this.rowsPerPage = Check.that(rowsPerPage).is(positive()).intValue();
    return this;
  }

  public PagingDataHelper selectFirstPage() {
    page = 0;
    first = 0;
    return this;
  }

  public PagingDataHelper selectPreviousPage() {
    page = Math.max(0, --page);
    if (page < first) {
      first = shift() ? page : nextWindow();
    }
    return this;
  }

  public PagingDataHelper selectNextPage() {
    page = clamp(++page);
    if (page >= first + windowSize) {
      if (shift()) {
        first = Math.max(0, page - windowSize + 1);
      } else {
        first = nextWindow();
      }
    }
    return this;
  }

  public PagingDataHelper selectLastPage() {
    Check.that(rowCount).is(ne(), -1, ERR_NO_ROW_COUNT);
    page = clamp(getPageCount());
    if (shift()) {
      first = Math.max(0, page - windowSize + 1);
    } else {
      first = nextWindow();
    }
    return this;
  }

  public PagingDataHelper setSelectedPage(int pageNo) {
    Check.that(rowCount).is(ne(), -1, ERR_NO_ROW_COUNT);
    page = clamp(pageNo);
    if (page < first) {
      // That's odd; the user could not have requested this page by clicking
      // one of the page links so has probably been tinkering with the URL,
      // which is fine by us
      first = shift() ? page : nextWindow();
    } else if (page >= first + windowSize) {
      if (shift()) {
        first = Math.max(0, page - windowSize + 1);
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
    int numLinks = Math.min(getPageCount() - first, windowSize);
    List<Tuple<Boolean, Integer>> tuples = new ArrayList<>(numLinks);
    for (int i = first; i < first + numLinks; ++i) {
      tuples.add(Tuple.of(i == page, i + 1));
    }
    return tuples;
  }

  private int nextWindow() {
    return clamp(page - (page % windowSize));
  }

  private int clamp(int pageNo) {
    return Math.min(getPageCount() - 1, Math.max(0, pageNo));
  }

  private boolean shift() {
    return scrollType == SHIFT_WINDOW;
  }

  private int getPageCount() {
    return (int) Math.ceil((double) rowCount / (double) rowsPerPage);
  }
}
