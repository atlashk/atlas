package org.atlas.infrastructure.persistence.jpa.impl.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "product_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaProductDetails extends JpaBaseEntity {

  @Id
  private Integer productId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  private JpaProduct product;

  @Column(name = "description")
  private String description;
}
