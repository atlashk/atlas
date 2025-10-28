package org.atlas.domain.user.repository;

import java.util.Optional;
import org.atlas.domain.user.entity.Cart;

public interface CartRepository {

  Optional<Cart> findByUserId(Integer userId);

  void insert(Cart cart);

  void update(Cart cart);
}
