package org.atlas.domain.user.saga.checkout;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.framework.cache.ApplicationCache;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.saga.checkout.CheckoutCommand;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.atlas.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.util.CollectionUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClearCartCommandHandler {

  private final CartRepository cartRepository;
  private final CacheService cacheService;

  @SagaCommandHandler(command = CheckoutCommand.CLEAR_CART)
  public SagaCommandResult clearCart(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JsonUtil.getInstance().toObject(
        sagaContext.get("data", LinkedHashMap.class), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    try {
      Cart cart = cartRepository.findByUserId(checkoutSagaData.getUser().getId())
          .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

      if (CollectionUtil.isEmpty(cart.getCartItems())) {
        throw new DomainException(DomainError.CART_EMPTY);
      }

      // Clear cart in DB
      cart.clear();
      cartRepository.update(cart);
      log.info("Successfully cleared cart in DB: sagaId={}, orderId={}, userId={}",
          sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), checkoutSagaData.getUser().getId());

      // Clear cart in cache
      cacheService.evict(ApplicationCache.CART, String.valueOf(checkoutSagaData.getUser().getId()));
      log.info("Successfully cleared cart in cache: sagaId={}, orderId={}, userId={}",
          sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), checkoutSagaData.getUser().getId());
    } catch (Exception e) {
      // Don't compensate the previous steps
      log.error("Failed to clear cart: sagaId={}, orderId={}, userId={}, error={}",
          sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), checkoutSagaData.getUser().getId(),
          e.getMessage(), e);
    }

    return SagaCommandResult.success();
  }
}
