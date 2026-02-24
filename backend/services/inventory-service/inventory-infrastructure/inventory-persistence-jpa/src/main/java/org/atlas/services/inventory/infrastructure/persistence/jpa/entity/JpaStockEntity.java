package org.atlas.services.inventory.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "stock")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaStockEntity extends JpaBaseEntity {

  @Id
  @Column(name = "product_id")
  private String productId;

  @Column(name = "available_quantity")
  private Integer availableQuantity;

  @Column(name = "reserved_quantity")
  private Integer reservedQuantity;
}
