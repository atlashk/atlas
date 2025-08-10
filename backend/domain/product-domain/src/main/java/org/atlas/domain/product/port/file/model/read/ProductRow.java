package org.atlas.domain.product.port.file.model.read;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import org.atlas.domain.product.shared.enums.ProductStatus;

@Data
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
