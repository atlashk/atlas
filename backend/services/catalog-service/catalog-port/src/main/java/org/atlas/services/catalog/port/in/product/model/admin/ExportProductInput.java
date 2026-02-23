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
import org.atlas.libs.framework.file.FileType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ExportProductInput {

  private String id;
  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private ProductType stockStatus;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private List<Integer> categoryIds;
  private FileType fileType;
}
