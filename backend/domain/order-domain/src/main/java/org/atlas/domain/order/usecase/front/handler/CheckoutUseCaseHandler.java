package org.atlas.domain.order.usecase.front.handler;

import java.time.Duration;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.domain.order.entity.PaymentEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.domain.order.mapper.OrderMapper;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.framework.cryptography.HashingUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.internalapi.user.CartApiClient;
import org.atlas.framework.internalapi.user.model.CartItemResponse;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;
import org.atlas.framework.lock.LockService;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.context.model.CheckoutSagaData;
import org.atlas.framework.saga.orchestrator.SagaOrchestrator;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.SequenceType;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class CheckoutUseCaseHandler {

  private final OrderRepository orderRepository;
  private final CartApiClient cartApiClient;
  private final LockService lockService;
  private final SequenceGenerator sequenceGenerator;
  private final SagaOrchestrator sagaOrchestrator;

  public Integer handle(CheckoutInput input) {
    // Fetch cart
    CartResponse cart = getCart(input.getUserId());
    if (CollectionUtil.isEmpty(cart.getCartItems())) {
      throw new DomainException(DomainError.CART_EMPTY);
    }

    // Checkout idempotence guarantee
    String lockKey = obtainLockKey(input, cart);
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofMinutes(15);
    boolean lockAcquired = lockService.acquireLock(lockKey, waitTime, leaseTime);
    if (!lockAcquired) {
      throw new DomainException(DomainError.CONFLICT,
          "Another checkout operation is already in progress.");
    }

    try {
      // Insert new order into DB
      OrderEntity order = newOrder(input, cart);
      orderRepository.insert(order);
      log.info("Order created successfully for user {}", input.getUserId());

      // Start saga
      CheckoutSagaData checkoutSagaData = OrderMapper.toCheckoutSagaData(order);
      Integer sagaId = sagaOrchestrator.startSaga("checkout",
          SagaContext.of("data", checkoutSagaData));
      order.setSagaId(sagaId);
      orderRepository.update(order);

      return order.getId();
    } finally {
      lockService.releaseLock(lockKey);
    }
  }

  private CartResponse getCart(Integer userId) {
    GetCartRequest request = GetCartRequest.builder().userId(userId).build();
    return cartApiClient.call(request);
  }

  private String obtainLockKey(CheckoutInput input, CartResponse cart) {
    // Create a deterministic signature based on order items
    StringBuilder signature = new StringBuilder();
    cart.getCartItems().stream()
        .sorted(Comparator.comparingInt(cartItem -> cartItem.getProduct().getId())) // Sort for consistency
        .forEach(cartItem -> signature.append(cartItem.getProduct().getId())
                .append(":")
                .append(cartItem.getQuantity())
                .append(";"));
    String hash = HashingUtil.sha256ToHex(signature.toString());
    return String.format("checkout:%d:%s", input.getUserId(), hash);
  }

  private OrderEntity newOrder(CheckoutInput input, CartResponse cart) {
    // Order
    OrderEntity order = new OrderEntity();
    order.setCode(sequenceGenerator.generate(SequenceType.ORDER));
    order.setStatus(OrderStatus.AWAITING_PRODUCT_RESERVATION);

    // User
    UserEntity user = UserEntity.builder()
        .id(input.getUserId())
        .build();
    order.setUser(user);

    // Order items
    for (CartItemResponse cartItem : cart.getCartItems()) {
      // Product
      ProductEntity product = ProductEntity.builder()
          .id(cartItem.getProduct().getId())
          .name(cartItem.getProduct().getName())
          .price(cartItem.getProduct().getPrice())
          .build();

      OrderItemEntity orderItem = OrderItemEntity.builder()
          .product(product)
          .quantity(cartItem.getQuantity())
          .build();

      order.addOrderItem(orderItem);
    }

    // Amount
    order.calculateOrderAmount();

    // Payment
    PaymentEntity payment = new PaymentEntity();
    payment.setMethod(input.getPaymentMethod());
    order.setPayment(payment);

    return order;
  }
}
