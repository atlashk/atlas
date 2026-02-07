package org.atlas.services.product.infrastructure.api.server.rest.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.product.ProductStockStatus;

@Schema(description = "Request object for retrieving product list")
@Getter
@Setter
public class AdminRetrieveProductListRequest {

  @Schema(description = "The unique identifier of the product", example = "1")
  private String id;

  @Schema(description = "Keyword for searching products", example = "T-Shirt")
  private String keyword;

  @Schema(description = "Minimum price for filtering products", example = "10.00")
  private BigDecimal minPrice;

  @Schema(description = "Maximum price for filtering products", example = "100.00")
  private BigDecimal maxPrice;

  @Schema(description = "Stock status", example = "IN_STOCK")
  private ProductStockStatus stockStatus;

  @Schema(description = "Date from which the product is available (ISO 8601 format)", example = "2023-01-01T00:00:00Z")
  private Date availableFrom;

  @Schema(description = "Indicates if the product is active", example = "true")
  private Boolean isActive;

  @Schema(description = "Brand ID for filtering products", example = "1")
  private Integer brandId;

  @Schema(description = "List of category IDs for filtering products", example = "[1, 2, 3]")
  private List<Integer> categoryIds;

  @Schema(description = "Page number for pagination", example = "1")
  private int page = 1;

  @Schema(description = "Number of items per page", example = "20")
  private int size = CommonConstant.DEFAULT_PAGE_SIZE;
}
