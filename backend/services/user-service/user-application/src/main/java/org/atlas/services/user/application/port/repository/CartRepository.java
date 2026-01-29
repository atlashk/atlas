package org.atlas.services.user.application.port.repository;

import java.util.Optional;
import org.atlas.services.user.domain.entity.Cart;

public interface CartRepository {

  Optional<Cart> findByUserId(Integer userId);

  void insert(Cart cart);

  void update(Cart cart);
}
