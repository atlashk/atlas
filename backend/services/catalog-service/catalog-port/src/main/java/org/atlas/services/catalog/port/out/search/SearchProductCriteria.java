package org.atlas.services.catalog.port.out.search;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SearchProductCriteria {

  private String keyword;

  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private String brandId;

  private List<String> categoryIds;
}
