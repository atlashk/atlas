package org.atlas.user.application.port.repository;

import java.util.Optional;
import org.atlas.user.domain.entity.Cart;

public interface CartRepository {

  Optional<Cart> findByUserId(Integer userId);

  void insert(Cart cart);

  void update(Cart cart);
}
