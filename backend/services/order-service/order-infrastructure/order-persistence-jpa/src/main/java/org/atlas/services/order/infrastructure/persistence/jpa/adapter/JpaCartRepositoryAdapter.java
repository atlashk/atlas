package org.atlas.services.order.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.mapper.JpaCartMapper;
import org.atlas.services.order.infrastructure.persistence.jpa.repository.JpaCartRepository;
import org.atlas.services.order.port.out.repository.CartRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCartRepositoryAdapter implements CartRepository {

  private final JpaCartRepository jpaCartRepository;

  @Override
  public Optional<CartEntity> findByUserId(String userId) {
    return jpaCartRepository.findByUserIdAndFetch(userId).map(JpaCartMapper.INSTANCE::toCart);
  }

  @Override
  public void insert(CartEntity cart) {
    JpaCartEntity jpaCart = JpaCartMapper.INSTANCE.toJpaCart(cart);
    jpaCartRepository.insert(jpaCart);
    cart.setId(jpaCart.getId());
  }

  @Override
  public void update(CartEntity cart) {
    JpaCartEntity jpaCart = JpaCartMapper.INSTANCE.toJpaCart(cart);
    JpaCartEntity saved = jpaCartRepository.save(jpaCart);
    cart.setId(saved.getId());
  }
}
