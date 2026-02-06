package org.atlas.libs.framework.paging;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.util.CollectionUtil;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PagingResult<T> {

  private List<T> data;
  private Pagination pagination;

  /* ---------- Factory methods ---------- */

  /**
   * Empty result
   */
  public static <T> PagingResult<T> empty() {
    return new PagingResult<>(Collections.emptyList(), Pagination.empty());
  }

  public static <T> PagingResult<T> of(List<T> data, Pagination pagination) {
    return new PagingResult<>(data, pagination);
  }
  
  /**
   * Case A: Offset pagination WITH count
   */
  public static <T> PagingResult<T> of(List<T> data, long totalRecords, PagingRequest request) {
    return new PagingResult<>(data, Pagination.countable(totalRecords, request));
  }

  /**
   * Case B: Offset pagination WITHOUT count (Keycloak)
   */
  public static <T> PagingResult<T> of(List<T> data, PagingRequest request, boolean hasNext) {
    return new PagingResult<>(data, Pagination.blindOffset(request, hasNext));
  }

  public boolean checkEmpty() {
    return CollectionUtil.isEmpty(data) || pagination.getTotalRecords() == 0L;
  }

  public <U> PagingResult<U> map(Function<? super T, ? extends U> mapper) {
    List<U> mapped = data.stream()
        .map(mapper)
        .collect(Collectors.toList());
    return new PagingResult<>(mapped, pagination);
  }
}
