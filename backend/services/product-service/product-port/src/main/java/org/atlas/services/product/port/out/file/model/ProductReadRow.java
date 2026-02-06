package org.atlas.services.product.port.out.file.model;

import java.math.BigDecimal;
import java.util.Date;
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
public class ProductReadRow {

  private String name;
  private BigDecimal price;
  private Integer quantity;
  private ProductStockStatus stockStatus;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private String categoryIds; // Split by |
}
