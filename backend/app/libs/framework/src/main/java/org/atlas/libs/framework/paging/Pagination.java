package org.atlas.libs.framework.paging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.util.PagingUtil;

@NoArgsConstructor
@Getter
@Setter
public class Pagination {

  /* ---------- Common ---------- */
  private int pageSize;
  private boolean hasNext;

  /* ---------- Offset (optional) ---------- */
  private int currentPage;

  /* ---------- Countable only ---------- */
  private long totalRecords;
  private int totalPages;

  /* ---------- Factories ---------- */

  public static Pagination empty() {
    return new Pagination();
  }

  /** Offset pagination WITH count */
  public static Pagination countable(
      long totalRecords,
      PagingRequest request
  ) {
    Pagination pagination = new Pagination();
    pagination.pageSize = request.getSize();
    pagination.currentPage = request.getPage();
    pagination.totalRecords = totalRecords;
    pagination.totalPages = PagingUtil.calcTotalPages(totalRecords, pagination.pageSize);
    pagination.hasNext = pagination.currentPage + 1 < pagination.totalPages;
    return pagination;
  }

  /** Offset pagination WITHOUT count */
  public static Pagination blindOffset(
      PagingRequest request,
      boolean hasNext
  ) {
    Pagination pagination = new Pagination();
    pagination.pageSize = request.getSize();
    pagination.currentPage = request.getPage();
    pagination.hasNext = hasNext;
    return pagination;
  }
}
