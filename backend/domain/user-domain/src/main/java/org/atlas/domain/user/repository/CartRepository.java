package org.atlas.domain.user.repository;

import java.util.Optional;
import org.atlas.domain.user.entity.CartEntity;

public interface CartRepository {

  Optional<CartEntity> findByUserId(Integer userId);

  void insert(CartEntity cartEntity);

  void update(CartEntity cartEntity);
}
