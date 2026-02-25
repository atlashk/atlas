package org.atlas.services.catalog.api.rest.product.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.api.rest.brand.model.BrandResponse;
import org.atlas.services.catalog.api.rest.category.model.CategoryResponse;

@Schema(description = "Response object for retrieving product details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  @Schema(description = "Unique identifier of the product", example = "123")
  private String id;

  @Schema(description = "Name of the product", example = "T-Shirt")
  private String name;

  @Schema(description = "Image of the product", example = "https://example.com/product-image.jpg")
  private String image;

  @Schema(description = "Price of the product", example = "19.99")
  private BigDecimal price;

  @Schema(description = "Brand information of the product")
  private BrandResponse brand;

  @Schema(description = "Detailed information about the product")
  private ProductDetailsResponse details;

  @Schema(description = "List of attributes associated with the product")
  private List<ProductAttributeResponse> attributes;

  @Schema(description = "List of categories the product belongs to")
  private List<CategoryResponse> categories;
}
