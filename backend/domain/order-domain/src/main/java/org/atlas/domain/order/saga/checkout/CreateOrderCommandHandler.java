package org.atlas.domain.order.saga.checkout;

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
import org.atlas.domain.order.usecase.front.model.FrontCheckoutInput;
import org.atlas.framework.cryptography.HashingUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.internalapi.user.CartApiClient;
import org.atlas.framework.internalapi.user.model.CartItemResponse;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;
import org.atlas.framework.lock.LockService;
import org.atlas.framework.saga.annotation.SagaCompensationHandler;
import org.atlas.framework.saga.annotation.SagaCommandHandler;
import org.atlas.framework.saga.command.CheckoutCommand;
import org.atlas.framework.saga.command.SagaCommandResult;
import org.atlas.framework.saga.context.CheckoutSagaData;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.SequenceType;
import org.atlas.framework.util.CollectionUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateOrderCommandHandler {

  private final OrderRepository orderRepository;
  private final CartApiClient cartApiClient;
  private final LockService lockService;
  private final SequenceGenerator sequenceGenerator;

  @SagaCommandHandler(command = CheckoutCommand.CREATE_ORDER)
  public SagaCommandResult createOrder(SagaCommand event) {
    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    FrontCheckoutInput input = sagaContext.get("input", FrontCheckoutInput.class);
    if (input == null) {
      throw new IllegalArgumentException("Checkout input is required");
    }

    // Fetch cart
    CartResponse cart = getCart(input.getUserId());
    if (CollectionUtil.isEmpty(cart.getItems())) {
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

    CheckoutSagaData checkoutSagaData;
    try {
      // Insert new order into DB
      OrderEntity order = newOrder(input, cart);
      orderRepository.insert(order);
      log.info("Order created successfully for user {}", input.getUserId());

      checkoutSagaData = OrderMapper.toCheckoutSagaData(order);
    } finally {
      lockService.releaseLock(lockKey);
    }

    return SagaCommandResult.builder()
        .success(true)
        .result(checkoutSagaData)
        .build();
  }

  private CartResponse getCart(Integer userId) {
    GetCartRequest request = GetCartRequest.builder().userId(userId).build();
    return cartApiClient.call(request);
  }

  private String obtainLockKey(FrontCheckoutInput input, CartResponse cart) {
    // Create a deterministic signature based on order items
    StringBuilder signature = new StringBuilder();
    cart.getItems().stream()
        .sorted(Comparator.comparingLong(CartItemResponse::getProductId)) // Sort for consistency
        .forEach(
            item -> signature.append(item.getProductId()).append(":").append(item.getQuantity())
                .append(";"));
    String hash = HashingUtil.sha256ToHex(signature.toString());
    return String.format("checkout:%d:%s", input.getUserId(), hash);
  }

  private OrderEntity newOrder(FrontCheckoutInput input, CartResponse cart) {
    // Order
    OrderEntity order = new OrderEntity();
    order.setCode(sequenceGenerator.generate(SequenceType.ORDER));
    order.setStatus(OrderStatus.AWAITING_PRODUCT_RESERVATION);

    // User
    UserEntity user = UserEntity.builder().id(input.getUserId()).build();
    order.setUser(user);

    // Order Items
    for (CartItemResponse cartItem : cart.getItems()) {
      // Product
      ProductEntity product = ProductEntity.builder().id(cartItem.getProductId()).build();

      OrderItemEntity orderItem = OrderItemEntity.builder().product(product)
          .quantity(cartItem.getQuantity()).build();

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

  @SagaCompensationHandler(command = CheckoutCommand.CREATE_ORDER)
  public void compensateCreateOrder(SagaCompensation event) {
    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    OrderEntity order = sagaContext.get("order", OrderEntity.class);
    if (order == null) {
      log.error("No order found in saga context for compensation.");
      return;
    }

    order.setStatus(OrderStatus.CANCELED);
    orderRepository.update(order);
    log.info("Order with ID {} has been canceled as part of compensation.", order.getId());
  }
}
