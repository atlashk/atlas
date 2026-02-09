package org.atlas.services.product.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.product.ProductStockStatus;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaProductEntity extends JpaBaseEntity {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "price")
  private BigDecimal price;

  @Column(name = "stock_status")
  @Enumerated(EnumType.STRING)
  private ProductStockStatus stockStatus;

  @Column(name = "quantity")
  private Integer quantity;

  @Column(name = "available_from")
  private Date availableFrom;

  @Column(name = "is_active")
  private Boolean isActive;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id")
  private JpaBrandEntity brand;

  @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private JpaProductDetailsEntity details;

  @OneToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      mappedBy = "product",
      orphanRemoval = true
  )
  private Set<JpaProductAttributeEntity> attributes = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "product_category",
      joinColumns = {@JoinColumn(name = "product_id")},
      inverseJoinColumns = {@JoinColumn(name = "category_id")}
  )
  private Set<JpaCategoryEntity> categories = new HashSet<>();

  public void addAttribute(JpaProductAttributeEntity attribute) {
    if (attributes == null) {
      attributes = new HashSet<>();
    }
    attribute.setProduct(this);
    attributes.add(attribute);
  }
}
