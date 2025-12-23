package org.atlas.application.order.service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.order.mapper.OrderMapper;
import org.atlas.application.order.model.CheckoutInput;
import org.atlas.application.order.model.RetrieveOrderListInput;
import org.atlas.application.order.model.RetrieveOrderStatusOutput;
import org.atlas.application.order.port.repository.OrderRepository;
import org.atlas.application.order.port.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.OrderItem;
import org.atlas.domain.order.entity.Order.PaymentSnapshot;
import org.atlas.domain.order.entity.Order.ProductSnapshot;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.collection.CollectionUtil;
import org.atlas.framework.cryptography.HashingUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.internalapi.user.CartApiClient;
import org.atlas.framework.internalapi.user.UserApiClient;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.lock.LockService;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.SequenceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final LockService lockService;
  private final UserApiClient userApiClient;
  private final CartApiClient cartApiClient;
  private final SagaOrchestrator sagaOrchestrator;
  private final SequenceGenerator sequenceGenerator;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<Order> retrieveOrderList(RetrieveOrderListInput input) {
    FindOrderCriteria criteria = OrderMapper.INSTANCE.toFindOrderCriteria(input);
    return orderRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  @Override
  @Transactional(readOnly = true)
  public RetrieveOrderStatusOutput retrieveOrderStatus(Integer orderId, Integer userId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    if (!Objects.equals(order.getCreatedBy(), userId)) {
      throw new DomainException(DomainError.FORBIDDEN);
    }

    return new RetrieveOrderStatusOutput(order.getStatus(), order.getCancellationReason());
  }

  @Override
  @Transactional
  public Integer checkout(CheckoutInput input) {
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
