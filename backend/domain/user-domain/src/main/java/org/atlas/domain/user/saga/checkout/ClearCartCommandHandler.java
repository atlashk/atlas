package org.atlas.domain.user.saga.checkout;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.framework.cache.ApplicationCache;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.saga.annotation.SagaCommandHandler;
import org.atlas.framework.saga.command.SagaCommandResult;
import org.atlas.framework.saga.command.model.CheckoutCommand;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.context.model.CheckoutSagaData;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
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
      CartEntity cart = cartRepository.findByUserId(checkoutSagaData.getUserId())
          .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

      if (!cart.hasItems()) {
        throw new DomainException(DomainError.CART_EMPTY);
      }

      // Clear cart in DB
      cart.clear();
      cartRepository.update(cart);
      log.info("Successfully cleared cart in DB: sagaId={}, orderId={}, userId={}",
          sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), checkoutSagaData.getUserId());

      // Clear cart in cache
      cacheService.evict(ApplicationCache.CART, String.valueOf(checkoutSagaData.getUserId()));
      log.info("Successfully cleared cart in cache: sagaId={}, orderId={}, userId={}",
          sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), checkoutSagaData.getUserId());
    } catch (Exception e) {
      // Don't compensate the previous steps
      log.error("Failed to clear cart: sagaId={}, orderId={}, userId={}, error={}",
          sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), checkoutSagaData.getUserId(),
          e.getMessage(), e);
    }

    return SagaCommandResult.success(null);
  }
}
