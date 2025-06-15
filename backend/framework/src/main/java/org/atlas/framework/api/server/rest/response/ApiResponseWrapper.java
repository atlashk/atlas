package org.atlas.framework.api.server.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.framework.paging.PagingResult;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseWrapper<T> {

  private boolean success;
  private T data;
  private Object metadata;
  private Integer errorCode;
  private String errorMessage;

  public static <T> ApiResponseWrapper<T> success() {
    return new ApiResponseWrapper<>(true, null, null, null, null);
  }

  public static <T> ApiResponseWrapper<T> success(T data) {
    return new ApiResponseWrapper<>(true, data, null, null, null);
  }

  public static <T> ApiResponseWrapper<T> success(T data, Object metadata) {
    return new ApiResponseWrapper<>(true, data, metadata, null, null);
  }

  public static <T> ApiResponseWrapper<List<T>> successPage(PagingResult<T> pagingResult) {
    if (pagingResult.getPagination().getTotalPages() > 0) {
      pagingResult.getPagination().setCurrentPage(pagingResult.getPagination().getCurrentPage() + 1);
    }
    return new ApiResponseWrapper<>(true, pagingResult.getData(), pagingResult.getPagination(),
        null, null);
  }

  public static ApiResponseWrapper<Void> error(int errorCode, String errorMessage) {
    return new ApiResponseWrapper<>(false, null, null, errorCode, errorMessage);
  }
}
