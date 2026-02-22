package org.atlas.services.catalog.port.out.file.model;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.catalog.ProductType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductWriteRow {

  private String id;
  private String name;
  private BigDecimal price;
  private Integer quantity;
  private ProductType stockStatus;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private String categoryIds;
}
