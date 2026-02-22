package org.atlas.services.catalog.port.in.product.model.admin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.catalog.ProductType;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveProductListInput {

  private String id;

  private String keyword;

  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private ProductType stockStatus;

  private Date availableFrom;

  private Boolean isActive;

  private Integer brandId;

  private List<Integer> categoryIds;

  private PagingRequest pagingRequest;
}
