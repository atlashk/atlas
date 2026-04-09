package org.atlas.services.catalog.api.rest.product.model.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductType;

@Schema(description = "Request object for creating a new product")
@Getter
@Setter
public class CreateProductRequest {

  @NotBlank
  @Schema(description = "Name of the product", example = "T-Shirt", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Type of the product", example = "PHYSICAL", requiredMode = RequiredMode.REQUIRED)
  private ProductType type;
  
  @NotNull
  @DecimalMin(value = "0.0")
  @Schema(description = "Price of the product", example = "19.99", requiredMode = RequiredMode.REQUIRED)
  private BigDecimal price;

  @NotNull
  @Schema(description = "Date and time the product was published (ISO 8601 format)", example = "2023-10-01T10:00:00Z", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime publishedAt;

  @NotNull
  @PositiveOrZero
  @Schema(description = "Quantity of the product available", example = "100", requiredMode = RequiredMode.REQUIRED)
  private Integer initialQuantity;

  @NotNull
  @Schema(description = "ID of the brand associated with the product", example = "BRD0001", requiredMode = RequiredMode.REQUIRED)
  private String brandId;

  @NotNull
  @Valid
  @Schema(description = "Detailed information about the product", requiredMode = RequiredMode.REQUIRED)
  private ProductDetails details;

  @Valid
  @Schema(description = "List of product attributes")
  private List<ProductAttribute> attributes;

  @NotEmpty
  @Schema(description = "List of category IDs the product belongs to", example = "[\"CAT0001\", \"CAT0002\", \"CAT0003\"]", requiredMode = RequiredMode.REQUIRED)
  private List<String> categoryIds;

  @Schema(description = "Detailed information about the product")
  @Getter
  @Setter
  public static class ProductDetails {

    @NotBlank
    @Schema(description = "Description of the product", example = "A comfortable cotton t-shirt", requiredMode = RequiredMode.REQUIRED)
    private String description;
  }

  @Schema(description = "Attributes associated with the product")
  @Getter
  @Setter
  public static class ProductAttribute {

    @NotBlank
    @Schema(description = "Name of the product attribute", example = "Color", requiredMode = RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Value of the product attribute", example = "Red", requiredMode = RequiredMode.REQUIRED)
    private String value;
  }
}
