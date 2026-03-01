package org.atlas.services.catalog.api.rest.product.model.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductType;

@Getter
@Setter
@Schema(description = "Request object for updating an existing product")
public class UpdateProductRequest {

  @NotBlank
  @Schema(description = "Name of the product", example = "T-Shirt", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Type of the product", example = "PHYSICAL", requiredMode = Schema.RequiredMode.REQUIRED)
  private ProductType type;
  
  @NotNull
  @DecimalMin(value = "0.0")
  @Schema(description = "Price of the product", example = "19.99", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal price = BigDecimal.ZERO;

  @NotNull
  @Schema(description = "Date and time the product was published (ISO 8601 format)", example = "2023-10-01T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
  private Date publishedAt;

  @NotNull
  @Schema(description = "ID of the brand associated with the product", example = "BRD0001", requiredMode = Schema.RequiredMode.REQUIRED)
  private String brandId;

  @NotNull
  @Valid
  @Schema(description = "Detailed information about the product", requiredMode = Schema.RequiredMode.REQUIRED)
  private ProductDetails details;

  @Valid
  @Schema(description = "List of product attributes")
  private List<ProductAttribute> attributes;

  @NotEmpty
  @Schema(description = "List of category IDs the product belongs to", example = "[\"CAT0001\", \"CAT0002\", \"CAT0003\"]", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<String> categoryIds;

  @Getter
  @Setter
  @Schema(description = "Detailed information about the product")
  public static class ProductDetails {

    @NotBlank
    @Schema(description = "Description of the product", example = "A comfortable cotton t-shirt", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
  }

  @Getter
  @Setter
  @Schema(description = "Attributes associated with the product")
  public static class ProductAttribute {

    @Schema(description = "Unique identifier of the product attribute", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @NotBlank
    @Schema(description = "Name of the product attribute", example = "Color", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Value of the product attribute", example = "Red", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;
  }
}
