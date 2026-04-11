package org.atlas.services.catalog.domain.entity;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;
import org.atlas.libs.framework.util.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Product extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private String name;

  private ProductType type;

  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  private String image;

  private LocalDateTime publishedAt;

  // Based on inventory-service stock status
  @Builder.Default
  private Boolean inStock = true;

  private Integer initialQuantity;

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
