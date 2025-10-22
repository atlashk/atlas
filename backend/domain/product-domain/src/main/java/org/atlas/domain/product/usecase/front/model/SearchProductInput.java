package org.atlas.domain.product.usecase.front.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.paging.PagingRequest;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductInput {

  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private Integer brandId;
  private List<Integer> categoryIds;
  private PagingRequest pagingRequest;
}
