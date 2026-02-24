package org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductType;
import org.atlas.services.catalog.infrastructure.api.server.rest.brand.model.BrandResponse;
import org.atlas.services.catalog.infrastructure.api.server.rest.category.model.CategoryResponse;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.ProductAttributeResponse;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.ProductDetailsResponse;

@Schema(description = "Response object for retrieving product details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  @Schema(description = "Unique identifier of the product", example = "PRD0000001")
  private String id;

  @Schema(description = "Name of the product", example = "T-Shirt")
  private String name;

  @Schema(description = "Type of the product", example = "PHYSICAL")
  private ProductType type;
  
  @Schema(description = "Image of the product", example = "https://example.com/product-image.jpg")
  private String image;

  @Schema(description = "Price of the product", example = "19.99")
  private BigDecimal price;

  @Schema(description = "Date and time the product was published in ISO 8601 format", example = "2023-10-01T10:00:00Z")
  private Date publishedAt;

  @Schema(description = "Indicates if the product is in stock", example = "true")
  private Boolean inStock;

  @Schema(description = "Brand information of the product")
  private BrandResponse brand;

  @Schema(description = "Detailed information about the product")
  private ProductDetailsResponse details;

  @Schema(description = "List of attributes associated with the product")
  private List<ProductAttributeResponse> attributes;

  @Schema(description = "List of categories the product belongs to")
  private List<CategoryResponse> categories;
}
