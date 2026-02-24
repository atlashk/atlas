package org.atlas.services.catalog.port.in.product.model.admin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductType;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveProductListInput {

  private String id;

  private String keyword;

  private ProductType type;
  
  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private Date startPublishedAt;

  private Date endPublishedAt;

  private Boolean inStock;

  private String brandId;

  private List<String> categoryIds;

  private PagingRequest pagingRequest;
}
