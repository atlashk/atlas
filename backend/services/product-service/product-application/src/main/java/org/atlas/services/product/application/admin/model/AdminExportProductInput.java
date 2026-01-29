package org.atlas.services.product.application.admin.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.product.ProductStatus;
import org.atlas.libs.framework.file.FileType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminExportProductInput {

  private Integer id;
  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private ProductStatus status;
  private Date availableFrom;
  private Boolean isActive;
  private Integer brandId;
  private List<Integer> categoryIds;
  private FileType fileType;
}
