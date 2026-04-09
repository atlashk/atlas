package org.atlas.services.catalog.port.in.product.model.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.file.FileType;
import org.atlas.services.catalog.domain.entity.ProductType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ExportProductInput {

  private String id;

  private String keyword;

  private ProductType type;

  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private LocalDateTime startPublishedDate;

  private LocalDateTime endPublishedDate;

  private Boolean inStock;

  private String brandId;

  private List<String> categoryIds;

  private FileType fileType;
}
