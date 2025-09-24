package org.atlas.domain.user.repository;

import java.util.Optional;
import org.atlas.domain.user.entity.CartEntity;

public interface CartRepository {

  Optional<CartEntity> findByUserId(Integer userId);

  Optional<CartEntity> findById(Integer id);

  void insert(CartEntity cartEntity);

  void update(CartEntity cartEntity);

  void delete(Integer id);

  void deleteByUserId(Integer userId);
}
