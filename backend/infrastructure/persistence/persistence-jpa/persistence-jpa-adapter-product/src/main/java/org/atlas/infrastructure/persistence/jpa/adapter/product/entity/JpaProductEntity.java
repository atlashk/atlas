package org.atlas.infrastructure.persistence.jpa.adapter.product.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.product.shared.enums.ProductStatus;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "product")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaProductEntity extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Integer id;

  private String name;

  private BigDecimal price;

  private Integer quantity;

  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  @Column(name = "available_from")
  private Date availableFrom;

  @Column(name = "is_active")
  private Boolean isActive;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id")
  private JpaBrandEntity brand;

  @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private JpaProductDetailsEntity details;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
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

  public void addCategory(JpaCategoryEntity category) {
    if (categories == null) {
      categories = new HashSet<>();
    }
    categories.add(category);
  }
}
