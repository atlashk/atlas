package org.atlas.framework.paging;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PagingResult<T> {

  protected List<T> data;
  protected Pagination pagination;

  public boolean checkEmpty() {
    return pagination.getTotalRecords() == 0L;
  }

  public static <T> PagingResult<T> empty() {
    return new PagingResult<>(Collections.emptyList(), Pagination.empty());
  }

  public static <T> PagingResult<T> of(List<T> data, Pagination pagination) {
    return new PagingResult<>(data, pagination);
  }

  public static <T> PagingResult<T> of(List<T> data, long totalRecords,
      PagingRequest pagingRequest) {
    return new PagingResult<>(data, Pagination.of(totalRecords, pagingRequest));
  }

  public <U> PagingResult<U> map(Function<? super T, ? extends U> mapper) {
    List<U> mappedData = data.stream()
        .map(mapper)
        .collect(Collectors.toList());
    return new PagingResult<>(mappedData, pagination);
  }

  @Data
  public static class Pagination {

    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalRecords;

    public static Pagination empty() {
      return new Pagination();
    }

    public static Pagination of(long totalRecords, PagingRequest pagingRequest) {
      Pagination pagination = new Pagination();
      pagination.setCurrentPage(pagingRequest.getPage());
      pagination.setPageSize(pagingRequest.getSize());
      pagination.setTotalPages(
          (int) Math.ceil((double) totalRecords / pagination.getPageSize()));
      pagination.setTotalRecords(totalRecords);
      return pagination;
    }
  }
}
