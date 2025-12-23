package org.atlas.application.product.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveProductListInput {

  // Product name, description, brand name, category name, attribute name/value
  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private Integer brandId;
  private List<Integer> categoryIds;
  private PagingRequest pagingRequest;
}