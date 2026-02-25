package org.atlas.services.catalog.api.rest.product.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput.Mode;

@Schema(description = "Request object for retrieving product list")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveProductListRequest {

  @Schema(description = "Keyword for searching products.", example = "T-Shirt")
  private String keyword;

  @Schema(description = "Minimum price for filtering products.", example = "10.00")
  private BigDecimal minPrice;

  @Schema(description = "Maximum price for filtering products.", example = "100.00")
  private BigDecimal maxPrice;

  @Schema(description = "Brand ID for filtering products.", example = "BRD0001")
  private String brandId;

  @Schema(description = "List of category IDs for filtering products.", example = "[\"CAT00001\", \"CAT00002\", \"CAT00003\"]")
  private List<String> categoryIds;

  @Schema(description = "Retrieve mode", example = "DATABASE")
  private Mode mode;

  @Positive
  @Min(1)
  @Schema(description = "The page number", example = "1", defaultValue = "1")
  @Builder.Default
  private int page = 1;

  @Positive
  @Min(0)
  @Schema(description = "The number of records per page", example = "20", defaultValue = "20")
  @Builder.Default
  private int size = CommonConstant.DEFAULT_PAGE_SIZE;
}
