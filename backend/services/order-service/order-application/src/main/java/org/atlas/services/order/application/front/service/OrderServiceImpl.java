package org.atlas.services.order.application.front.service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.internalapi.iam.client.UserApiClient;
import org.atlas.libs.framework.internalapi.iam.model.ListUserRequest;
import org.atlas.libs.framework.internalapi.iam.model.UserResponse;
import org.atlas.libs.framework.lock.LockService;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.services.order.application.front.mapper.OrderMapper;
import org.atlas.services.order.domain.entity.Cart;
import org.atlas.services.order.domain.entity.CartItem;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.domain.entity.Order.OrderItem;
import org.atlas.services.order.domain.entity.Order.PaymentSnapshot;
import org.atlas.services.order.domain.entity.Order.ProductSnapshot;
import org.atlas.services.order.port.in.front.model.CheckoutInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderListInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderStatusOutput;
import org.atlas.services.order.port.in.front.service.CartService;
import org.atlas.services.order.port.in.front.service.OrderService;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.atlas.services.order.port.out.repository.criteria.FindOrderCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final UserApiClient userApiClient;
  private final CartService cartService;
  private final LockService lockService;
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

    if (!Objects.equals(order.getUser().getId(), userId)) {
      throw new DomainException(DomainError.FORBIDDEN);
    }

    return new RetrieveOrderStatusOutput(order.getStatus(), order.getCancellationReason());
  }

  @Override
  @Transactional
  public Integer checkout(CheckoutInput input) {
    // Retrieve user
    Integer userId = Contexts.getUserId();
    UserResponse userResponse = retrieveUser(userId);

    // Retrieve cart
    Cart cart = cartService.retrieveCart(userId);
    if (cart.isEmpty()) {
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
      Order order = newOrder(input, userResponse, cart);
      orderRepository.insert(order);
      log.info("Order created successfully for user {}", userId);

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

  private UserResponse retrieveUser(Integer userId) {
    ListUserRequest request = new ListUserRequest(List.of(userId));
    List<UserResponse> userResponses = userApiClient.call(request);
    if (CollectionUtil.isEmpty(userResponses)) {
      throw new DomainException(DomainError.USER_NOT_FOUND);
    }
    return userResponses.get(0);
  }

  private String obtainLockKey(CheckoutInput input, Cart cart) {
    // Create a deterministic signature based on order items
    StringBuilder signature = new StringBuilder();
    cart.getCartItems().stream().sorted(
            Comparator.comparingInt(cartItem -> cartItem.getProduct().getId())) // Sort for consistency
        .forEach(cartItem -> signature.append(cartItem.getProduct().getId()).append(":")
            .append(cartItem.getQuantity()).append(";"));
    String hash = HashingUtil.sha256ToHex(signature.toString());
    return String.format("checkout:%d:%s", cart.getUserId(), hash);
  }

  private Order newOrder(CheckoutInput input, UserResponse userResponse, Cart cart) {
    // Order
    Order order = new Order();
    order.setCode(sequenceGenerator.generate(SequenceType.ORDER));
    order.setStatus(OrderStatus.AWAITING_PRODUCT_RESERVATION);

    // User
    order.setUser(OrderMapper.INSTANCE.toUserSnapshot(userResponse));

    // Address
    order.setAddress(OrderMapper.INSTANCE.toAddress(input.getAddress()));

    // Order items
    for (CartItem cartItem : cart.getCartItems()) {
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
