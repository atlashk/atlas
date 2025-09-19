package org.atlas.infrastructure.api.server.rest.impl.product.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.atlas.domain.product.shared.enums.ProductStatus;

@Data
@Schema(description = "Request object for creating a new product.")
public class AdminCreateProductRequest {

  @NotBlank
  @Schema(description = "Name of the product.", example = "T-Shirt", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @DecimalMin(value = "0.0")
  @Schema(description = "Price of the product.", example = "19.99", requiredMode = RequiredMode.REQUIRED)
  private BigDecimal price;

  @Schema(description = "Base64 string of the product's image.", example = "data:image/jpeg;base64,..")
  private String image;

  @NotNull
  @PositiveOrZero
  @Schema(description = "Quantity of the product available.", example = "100", requiredMode = RequiredMode.REQUIRED)
  private Integer quantity;

  @NotNull
  @Schema(description = "Status of the product.", example = "IN_STOCK", requiredMode = RequiredMode.REQUIRED)
  private ProductStatus status;

  @NotNull
  @Schema(description = "Date and time the product becomes available in ISO 8601 format.", example = "2023-10-01T10:00:00Z", requiredMode = RequiredMode.REQUIRED)
  private Date availableFrom;

  @NotNull
  @Schema(description = "Indicates if the product is active.", example = "true", requiredMode = RequiredMode.REQUIRED)
  private Boolean isActive;

  @NotNull
  @Schema(description = "ID of the brand associated with the product.", example = "1", requiredMode = RequiredMode.REQUIRED)
  private Integer brandId;

  @NotNull
  @Valid
  @Schema(description = "Detailed information about the product.", requiredMode = RequiredMode.REQUIRED)
  private ProductDetails details;

  @Valid
  @Schema(description = "List of product attributes.")
  private List<ProductAttribute> attributes;

  @NotEmpty
  @Schema(description = "List of category IDs the product belongs to.", example = "[1, 2, 3]", requiredMode = RequiredMode.REQUIRED)
  private List<Integer> categoryIds;

  @Data
  @Schema(description = "Detailed information about the product.")
  public static class ProductDetails {

    @NotBlank
    @Schema(description = "Description of the product.", example = "A comfortable cotton t-shirt.", requiredMode = RequiredMode.REQUIRED)
    private String description;
  }

  @Data
  @Schema(description = "Attributes associated with the product.")
  public static class ProductAttribute {

    @NotBlank
    @Schema(description = "Name of the product attribute.", example = "Color", requiredMode = RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Value of the product attribute.", example = "Red", requiredMode = RequiredMode.REQUIRED)
    private String value;
  }
}
