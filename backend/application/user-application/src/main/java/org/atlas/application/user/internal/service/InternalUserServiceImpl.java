package org.atlas.application.user.internal.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.application.user.aggregator.CartAggregator;
import org.atlas.application.user.internal.model.InternalRetrieveUserListInput;
import org.atlas.application.user.port.repository.CartRepository;
import org.atlas.application.user.port.repository.UserRepository;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalUserServiceImpl implements InternalUserService {

  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final CartAggregator cartAggregator;

  @Override
  public Cart retrieveCart(Integer userId) {
    // Get or create cart for user
    Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
    if (cartOpt.isEmpty()) {
      return Cart.builder()
          .userId(userId)
          .build();
    }
    Cart cart = cartOpt.get();

    // Fetch products
    boolean allProductsAreValid = cartAggregator.aggregate(cart);

    // Update cart if necessary
    if (!allProductsAreValid) {
      cartRepository.update(cart);
    }

    return cart;
  }

  @Override
  public List<User> retrieveUserList(InternalRetrieveUserListInput input) {
    return userRepository.findByIdIn(input.getIds());
  }
}
