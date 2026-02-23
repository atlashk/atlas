package org.atlas.services.catalog.port.out.file.model;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductReadRow {

  private String name;
  private ProductType type;
  private BigDecimal price;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private String categoryIds; // Split by |
}
