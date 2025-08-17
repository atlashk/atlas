package org.atlas.infrastructure.persistence.jpa.core.paging;

import lombok.experimental.UtilityClass;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PagingConverter {

  public static Pageable convert(PagingRequest pagingRequest) {
    if (pagingRequest.hasSort()) {
      Sort.Direction sortDirection = pagingRequest.isSortAscending() ?
          Sort.Direction.ASC : Sort.Direction.DESC;
      Sort sort = Sort.by(sortDirection, pagingRequest.getSortBy());
      return PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize(), sort);
    } else {
      return PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize());
    }
  }

  public static <T> PagingResult<T> convert(org.springframework.data.domain.Page<T> springPage) {
    if (springPage.isEmpty()) {
      return PagingResult.empty();
    }
    PagingRequest pagingRequest = PagingRequest.of(springPage.getNumber(), springPage.getSize());
    return PagingResult.of(springPage.getContent(), springPage.getTotalElements(), pagingRequest);
  }
}
