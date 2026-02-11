package org.atlas.services.product.domain.entity;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.entity.DomainEntity;
import org.atlas.libs.framework.domain.product.ProductStockStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ProductEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private String name;

  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  // Don't sync to DB
  private String image;

  private ProductStockStatus stockStatus;

  private Integer quantity;

  private Date availableFrom;

  private Boolean isActive;

  // Associations

  // One-To-One
  @Valid
  private ProductDetailsEntity details;

  // One-To-Many
  @Valid
  private List<ProductAttributeEntity> attributes;

  // Many-To-One
  private BrandEntity brand;

  // Many-To-Many
  private List<CategoryEntity> categories;

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
    if (quantity == 0) {
      stockStatus = ProductStockStatus.OUT_STOCK;
    }
  }

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
