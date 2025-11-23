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

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Product extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private String name;

  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  // Don't sync to DB
  private String image;

  private Integer quantity;

  private ProductStatus status;

  private Date availableFrom;

  private Boolean isActive;

  // Associations

  // One-To-One
  @Valid
  private ProductDetails details;

  // One-To-Many
  @Valid
  private List<ProductAttribute> attributes;

  // Many-To-One
  private Brand brand;

  // Many-To-Many
  private List<Category> categories;

  public void addAttribute(ProductAttribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
  }

  public void addCategory(Category category) {
    if (categories == null) {
      categories = new ArrayList<>();
    }
    categories.add(category);
  }
}
