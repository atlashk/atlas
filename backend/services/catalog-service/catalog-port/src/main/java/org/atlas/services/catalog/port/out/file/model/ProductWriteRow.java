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
public class ProductWriteRow {

  private String id;
  private String name;
  private ProductType type;
  private BigDecimal price;
  private Boolean isActive;
  private Date availableFrom;
  private String brandName;
  private String categoryNames;
  private Integer quantity;
}
