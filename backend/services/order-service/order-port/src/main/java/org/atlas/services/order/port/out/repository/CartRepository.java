package org.atlas.services.order.port.out.repository;

import java.util.Optional;
import org.atlas.services.order.domain.entity.CartEntity;

public interface CartRepository {

  Optional<CartEntity> findByUserId(String userId);

  void insert(CartEntity cart);

  void update(CartEntity cart);
}
