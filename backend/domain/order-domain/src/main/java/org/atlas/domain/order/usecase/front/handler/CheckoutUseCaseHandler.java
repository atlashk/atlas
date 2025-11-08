package org.atlas.domain.order.usecase.front.handler;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.OrderItem;
import org.atlas.domain.order.entity.Order.PaymentSnapshot;
import org.atlas.domain.order.entity.Order.ProductSnapshot;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.domain.order.usecase.front.mapper.OrderMapper;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.framework.cryptography.HashingUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.internalapi.user.CartApiClient;
import org.atlas.framework.internalapi.user.UserApiClient;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.lock.LockService;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.SequenceType;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class CheckoutUseCaseHandler {

  private final OrderRepository orderRepository;
  private final CartApiClient cartApiClient;
  private final UserApiClient userApiClient;
  private final LockService lockService;
  private final SequenceGenerator sequenceGenerator;
  private final SagaOrchestrator sagaOrchestrator;

  public Integer handle(CheckoutInput input) {
    // Fetch user
    UserResponse userResponse = fetchUser(input.getUserId());

    // Fetch cart
    CartResponse cartResponse = fetchCart(input.getUserId());
    if (CollectionUtil.isEmpty(cartResponse.getCartItems())) {
      throw new DomainException(DomainError.CART_EMPTY);
    }

    // Checkout idempotence guarantee
    String lockKey = obtainLockKey(input, cartResponse);
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofMinutes(15);
    boolean lockAcquired = lockService.acquireLock(lockKey, waitTime, leaseTime);
    if (!lockAcquired) {
      throw new DomainException(DomainError.CONFLICT,
          "Another checkout operation is already in progress.");
    }

    try {
      // Insert new order into DB
      Order order = newOrder(input, userResponse, cartResponse);
      orderRepository.insert(order);
      log.info("Order created successfully for user {}", input.getUserId());

      // Start saga
      CheckoutSagaData checkoutSagaData = OrderMapper.INSTANCE.toCheckoutSagaData(order);
      Integer sagaId = sagaOrchestrator.runSaga("checkout",
          SagaContext.of("data", checkoutSagaData));

      // Update order saga_id
      order.setSagaId(sagaId);
      orderRepository.update(order);

      return order.getId();
    } finally {
      lockService.releaseLock(lockKey);
    }
  }

  private UserResponse fetchUser(Integer userId) {
    ListUserRequest request = new ListUserRequest(List.of(userId));
    List<UserResponse> userResponses = userApiClient.call(request);
    if (CollectionUtil.isEmpty(userResponses)) {
      throw new DomainException(DomainError.USER_NOT_FOUND);
    }
    return userResponses.get(0);
  }

  private CartResponse fetchCart(Integer userId) {
    GetCartRequest request = GetCartRequest.builder().userId(userId).build();
    return cartApiClient.call(request);
  }

  private String obtainLockKey(CheckoutInput input, CartResponse cart) {
    // Create a deterministic signature based on order items
    StringBuilder signature = new StringBuilder();
    cart.getCartItems().stream().sorted(
            Comparator.comparingInt(cartItem -> cartItem.getProduct().getId())) // Sort for consistency
        .forEach(cartItem -> signature.append(cartItem.getProduct().getId()).append(":")
            .append(cartItem.getQuantity()).append(";"));
    String hash = HashingUtil.sha256ToHex(signature.toString());
    return String.format("checkout:%d:%s", input.getUserId(), hash);
  }

  private Order newOrder(CheckoutInput input, UserResponse userResponse,
      CartResponse cartResponse) {
    // Order
    Order order = new Order();
    order.setCode(sequenceGenerator.generate(SequenceType.ORDER));
    order.setStatus(OrderStatus.AWAITING_PRODUCT_RESERVATION);

    // User
    order.setUser(OrderMapper.INSTANCE.toUserSnapshot(userResponse));

    // Address
    order.setAddress(OrderMapper.INSTANCE.toAddress(input.getAddress()));

    // Order items
    for (CartResponse.CartItem cartItem : cartResponse.getCartItems()) {
      // Product
      ProductSnapshot product = OrderMapper.INSTANCE.toProductSnapshot(cartItem.getProduct());

      OrderItem orderItem = OrderItem.builder()
          .product(product)
          .quantity(cartItem.getQuantity())
          .build();
      order.getOrderItems().add(orderItem);
    }

    // Amount
    order.calculateOrderAmount();

    // Payment snapshot
    PaymentSnapshot payment = PaymentSnapshot.builder()
        .paymentGatewayId(input.getPaymentGatewayId())
        .build();
    order.setPayment(payment);

    return order;
  }
}
