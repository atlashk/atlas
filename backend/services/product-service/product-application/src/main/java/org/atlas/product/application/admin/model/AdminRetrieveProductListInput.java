package org.atlas.product.application.admin.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.domain.product.ProductStatus;
import org.atlas.common.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminRetrieveProductListInput {

  private Integer id;
  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private ProductStatus status;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private List<Integer> categoryIds;
  private PagingRequest pagingRequest;
}


