package org.atlas.services.product.infrastructure.api.server.rest.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput.Mode;

@Schema(description = "Request object for retrieving product list")
@Getter
@Setter
public class RetrieveProductListRequest {

  @Schema(description = "Keyword for searching products.", example = "T-Shirt")
  private String keyword;

  @Schema(description = "Minimum price for filtering products.", example = "10.00")
  private BigDecimal minPrice;

  @Schema(description = "Maximum price for filtering products.", example = "100.00")
  private BigDecimal maxPrice;

  @Schema(description = "Brand ID for filtering products.", example = "1")
  private Integer brandId;

  @Schema(description = "List of category IDs for filtering products.", example = "[1, 2, 3]")
  private List<Integer> categoryIds;

  @Schema(description = "Retrieve mode", example = "DATABASE")
  private Mode mode;

  @Schema(description = "Page number for pagination.", example = "1")
  private int page = 1;

  @Schema(description = "Number of items per page.", example = "20")
  private int size = CommonConstant.DEFAULT_PAGE_SIZE;
}
