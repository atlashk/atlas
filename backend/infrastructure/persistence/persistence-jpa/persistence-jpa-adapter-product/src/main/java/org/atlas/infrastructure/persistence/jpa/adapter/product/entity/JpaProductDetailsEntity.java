package org.atlas.infrastructure.persistence.jpa.adapter.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "product_details")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaProductDetailsEntity extends JpaBaseEntity {

  @Id
  private Integer productId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  private JpaProductEntity product;

  private String description;
}
