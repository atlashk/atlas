package org.atlas.framework.objectmapper;

import java.util.List;
import java.util.function.Function;
import org.apache.commons.collections4.CollectionUtils;
import org.atlas.framework.paging.PagingResult;

public interface ObjectMapperService {

  <D> D map(Object source, Class<D> destinationType);

  default <D> List<D> mapList(List<?> source, Class<D> destinationType) {
    if (CollectionUtils.isEmpty(source)) {
      return List.of();
    }
    return source.stream()
        .map(it -> it != null ? map(it, destinationType) : null)
        .toList();
  }

  default <S, D> List<D> mapList(List<S> source, Function<S, D> mapper) {
    if (CollectionUtils.isEmpty(source)) {
      return List.of();
    }
    return source.stream()
        .map(mapper)
        .toList();
  }

  default <D> PagingResult<D> mapPage(PagingResult<?> source, Class<D> destinationType) {
    if (source.checkEmpty()) {
      return PagingResult.empty();
    }
    List<D> mappedData = mapList(source.getData(), destinationType);
    return PagingResult.of(mappedData, source.getPagination());
  }

  default <S, D> PagingResult<D> mapPage(PagingResult<S> source, Function<S, D> mapper) {
    if (source.checkEmpty()) {
      return PagingResult.empty();
    }
    List<D> mappedData = mapList(source.getData(), mapper);
    return PagingResult.of(mappedData, source.getPagination());
  }

  void merge(Object source, Object destination);
}
