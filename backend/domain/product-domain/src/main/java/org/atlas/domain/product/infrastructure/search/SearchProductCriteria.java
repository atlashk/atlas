package org.atlas.domain.product.infrastructure.search;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchProductCriteria {

  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private List<Integer> brandIds;
  private List<Integer> categoryIds;
}
