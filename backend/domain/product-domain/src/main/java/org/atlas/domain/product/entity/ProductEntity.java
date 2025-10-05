package org.atlas.domain.product.entity;

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
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ProductEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private String name;

  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  private String image;

  private Integer quantity;

  private ProductStatus status;

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
