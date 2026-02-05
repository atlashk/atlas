package org.atlas.services.order.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "cart")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaCart extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id")
  private String userId;

  @OneToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      mappedBy = "cart",
      orphanRemoval = true
  )
  private List<JpaCartItem> cartItems = new ArrayList<>();

  public void addCartItem(JpaCartItem cartItem) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }
    cartItem.setCart(this);
    cartItems.add(cartItem);
  }
}
