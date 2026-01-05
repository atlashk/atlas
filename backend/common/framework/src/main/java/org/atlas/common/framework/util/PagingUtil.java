package org.atlas.common.framework.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PagingUtil {

  public static int calcTotalPages(long totalRecords, long pageSize) {
    return (int) Math.ceil((double) totalRecords / pageSize);
  }
}
