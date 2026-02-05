package org.atlas.services.product.port.out.repository.criteria;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.product.ProductStockStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FindProductCriteria {

  private String productId;
  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private ProductStockStatus status;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private List<Integer> categoryIds;
}
