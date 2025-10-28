package org.atlas.infrastructure.persistence.jpa.impl.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCart;
import org.atlas.infrastructure.persistence.jpa.impl.user.mapper.JpaCartMapper;
import org.atlas.infrastructure.persistence.jpa.impl.user.repository.JpaCartRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCartRepositoryAdapter implements CartRepository {

  private final JpaCartRepository jpaCartRepository;

  @Override
  public Optional<Cart> findByUserId(Integer userId) {
    return jpaCartRepository.findByUserIdAndFetch(userId)
        .map(JpaCartMapper.INSTANCE::toCart);
  }

  @Override
  public void insert(Cart cart) {
    JpaCart jpaCart = JpaCartMapper.INSTANCE.toJpaCart(cart);
    jpaCartRepository.insert(jpaCart);
    cart.setId(jpaCart.getId());
  }

  @Override
  public void update(Cart cart) {
    JpaCart jpaCart = JpaCartMapper.INSTANCE.toJpaCart(cart);
    jpaCartRepository.save(jpaCart);
  }
}
