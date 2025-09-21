package org.atlas.framework.search.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCriteria {

  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private List<Integer> brandIds;
  private List<Integer> categoryIds;
}
