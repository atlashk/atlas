package org.atlas.services.user.persistence.jpa.adapter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.atlas.services.user.application.port.repository.CartRepository;
import org.atlas.services.user.domain.entity.Cart;
import org.atlas.services.user.domain.entity.CartItem;
import org.atlas.services.user.persistence.jpa.entity.JpaCart;
import org.atlas.services.user.persistence.jpa.entity.JpaCartItem;
import org.atlas.services.user.persistence.jpa.mapper.JpaCartMapper;
import org.atlas.services.user.persistence.jpa.repository.JpaCartRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCartRepositoryAdapter implements CartRepository {

  private final JpaCartRepository jpaCartRepository;

  @Override
  public Optional<Cart> findByUserId(Integer userId) {
    return jpaCartRepository.findByUserIdAndFetch(userId).map(JpaCartMapper.INSTANCE::toCart);
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
    JpaCart saved = jpaCartRepository.save(jpaCart);

    // Reflect generated IDs back into the passed entities
    cart.setId(saved.getId());
    if (saved.getCartItems() != null && cart.getCartItems() != null) {
      Map<Integer, Integer> savedCartItemIdByProductId = saved.getCartItems().stream()
          .collect(Collectors.toMap(JpaCartItem::getProductId, JpaCartItem::getId, (a, b) -> a));
      for (CartItem cartItem : cart.getCartItems()) {
        Integer productId = cartItem.getProduct() != null ? cartItem.getProduct().getId() : null;
        if (productId != null) {
          cartItem.setId(savedCartItemIdByProductId.get(productId));
        }
      }
    }
  }
}
