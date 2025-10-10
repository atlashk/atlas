package org.atlas.domain.product.infrastructure.file.model.read;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.product.shared.ProductStatus;

@Getter
@Setter
public class ProductRow {

  private String name;
  private BigDecimal price;
  private Integer quantity;
  private ProductStatus status;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private String categoryIds; // Split by |
}
