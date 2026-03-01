package org.atlas.services.catalog.api.rest.product.model.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.file.FileType;
import org.atlas.services.catalog.domain.entity.ProductType;

@Schema(description = "Request object for exporting products")
@Getter
@Setter
public class ExportProductRequest {

  @Schema(description = "The unique identifier of the product", example = "PRD0000001")
  private String id;

  @Schema(description = "Keyword for searching products", example = "T-Shirt")
  private String keyword;

  @Schema(description = "Type of the product", example = "PHYSICAL")
  private ProductType type;
  
  @Schema(description = "Minimum price for filtering products", example = "10.00")
  private BigDecimal minPrice;

  @Schema(description = "Maximum price for filtering products", example = "100.00")
  private BigDecimal maxPrice;

  @Schema(description = "Start date for filtering products by published date (ISO 8601 format)", example = "2023-01-01T00:00:00Z")
  private Date startPublishedAt;

  @Schema(description = "End date for filtering products by published date (ISO 8601 format)", example = "2023-12-31T23:59:59Z")
  private Date endPublishedAt;

  @Schema(description = "Indicates if the product is in stock", example = "true")
  private Boolean inStock;

  @Schema(description = "Brand ID for filtering products", example = "BRD0001")
  private String brandId;

  @Schema(description = "List of category IDs for filtering products", example = "[\"CAT0001\", \"CAT0002\", \"CAT0003\"]")
  private List<String> categoryIds;

  @Schema(description = "The type of the file to export to (e.g., csv, xlsx)", example = "CSV")
  private FileType fileType;
}
