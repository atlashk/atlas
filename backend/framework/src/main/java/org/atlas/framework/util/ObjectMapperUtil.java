package org.atlas.framework.util;

import java.util.List;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.atlas.framework.paging.PagingResult;

@UtilityClass
public class ObjectMapperUtil {

  public static <S, D> List<D> mapList(List<S> source, Function<S, D> mapper) {
    if (CollectionUtils.isEmpty(source)) {
      return List.of();
    }
    return source.stream()
        .map(mapper)
        .toList();
  }

  public static <S, D> PagingResult<D> mapPage(PagingResult<S> source, Function<S, D> mapper) {
    if (source.checkEmpty()) {
      return PagingResult.empty();
    }
    List<D> mappedData = mapList(source.getData(), mapper);
    return PagingResult.of(mappedData, source.getPagination());
  }
}
