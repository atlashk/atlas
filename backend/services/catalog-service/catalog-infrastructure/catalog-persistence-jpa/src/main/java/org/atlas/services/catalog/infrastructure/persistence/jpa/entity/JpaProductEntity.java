package org.atlas.services.catalog.infrastructure.persistence.jpa.entity;

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
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;
import org.atlas.services.catalog.domain.entity.ProductType;

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
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private ProductType type;

  @Column(name = "price")
  private BigDecimal price;

  @Column(name = "published_at")
  private Date publishedAt;

  @Column(name = "in_stock")
  private Boolean inStock;

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
  @Builder.Default
  private Set<JpaProductAttributeEntity> attributes = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "product_category",
      joinColumns = {@JoinColumn(name = "product_id")},
      inverseJoinColumns = {@JoinColumn(name = "category_id")}
  )
  @Builder.Default
  private Set<JpaCategoryEntity> categories = new HashSet<>();

  public void addAttribute(JpaProductAttributeEntity attribute) {
    if (attributes == null) {
      attributes = new HashSet<>();
    }
    attribute.setProduct(this);
    attributes.add(attribute);
  }
}
