package org.atlas.domain.product.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.domain.product.shared.enums.ProductStatus;
import org.atlas.framework.domain.entity.DomainEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ProductEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  @NotBlank
  private String name;

  @NotNull
  @DecimalMin(value = "0.0")
  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  private String image;

  @NotNull
  @PositiveOrZero
  private Integer quantity;

  @NotNull
  private ProductStatus status;

  @NotNull
  private Date availableFrom;

  private Boolean isActive;

  // Associations

  // One-To-One
  @NotNull
  @Valid
  private ProductDetailsEntity details;

  // One-To-Many
  @Valid
  private List<ProductAttributeEntity> attributes;

  // Many-To-One
  @NotNull
  private BrandEntity brand;

  // Many-To-Many
  @NotEmpty
  private List<CategoryEntity> categories;

  public void addAttribute(ProductAttributeEntity attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
  }

  public void addCategory(CategoryEntity category) {
    if (categories == null) {
      categories = new ArrayList<>();
    }
    categories.add(category);
  }
}
