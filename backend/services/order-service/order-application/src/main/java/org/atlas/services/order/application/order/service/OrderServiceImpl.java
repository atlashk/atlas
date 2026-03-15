package org.atlas.services.order.application.order.service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.internal.identity.client.UserApiClient;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.lock.LockService;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.application.order.mapper.OrderMapper;
import org.atlas.services.order.domain.entity.CartItemEntity;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.domain.entity.OrderEntity.OrderItem;
import org.atlas.services.order.domain.entity.OrderEntity.PaymentSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.ProductSnapshot;
import org.atlas.services.order.domain.error.DomainError;
import org.atlas.services.order.domain.exception.DomainException;
import org.atlas.services.order.port.in.cart.service.CartService;
import org.atlas.services.order.port.in.order.model.CheckoutInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderListInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderStatusOutput;
import org.atlas.services.order.port.in.order.service.OrderService;
import org.atlas.services.order.port.out.repository.OrderRepository;
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
  public PagingResult<OrderEntity> retrieveOrderList(RetrieveOrderListInput input) {
    OrderRepository.FindOrderCriteria criteria = OrderMapper.INSTANCE.toFindOrderCriteria(input);
    criteria.setUserId(Contexts.getUserId());
    return orderRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  @Override
  @Transactional(readOnly = true)
  public RetrieveOrderStatusOutput retrieveOrderStatus(String id) {
    String userId = Contexts.getUserId();

    OrderEntity order = orderRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    if (!Objects.equals(order.getUser().getId(), userId)) {
      throw new DomainException(CommonDomainError.FORBIDDEN);
    }

    return new RetrieveOrderStatusOutput(order.getStatus(), order.getCancellationReason());
  }

  @Override
  @Transactional
  public String checkout(CheckoutInput input) {
    // Retrieve user
    String userId = Contexts.getUserId();
    UserOutput user = retrieveUser(userId);

    // Retrieve cart
    List<CartItemEntity> cartItems = cartService.retrieveCart();
    if (CollectionUtil.isEmpty(cartItems)) {
      throw new DomainException(DomainError.CART_EMPTY);
    }

    // Checkout idempotence guarantee
    String lockKey = obtainLockKey(userId, cartItems);
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofMinutes(15);
    boolean lockAcquired = lockService.acquireLock(lockKey, waitTime, leaseTime);
    if (!lockAcquired) {
      throw new DomainException(CommonDomainError.CONFLICT,
          "Another checkout operation is already in progress.");
    }

    try {
      // Insert new order into DB
      OrderEntity order = newOrder(input, user, cartItems);
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

  private UserOutput retrieveUser(String userId) {
    RetrieveUserListInput request = new RetrieveUserListInput(List.of(userId));
    List<UserOutput> users = userApiClient.call(request);
    if (CollectionUtil.isEmpty(users)) {
      throw new DomainException(CommonDomainError.BAD_REQUEST, "User not found");
    }
    return users.get(0);
  }

  private String obtainLockKey(String userId, List<CartItemEntity> cartItems) {
    StringBuilder signature = new StringBuilder();
    cartItems.stream().sorted(
            Comparator.comparing(cartItem -> cartItem.getProduct().getId())) // Sort for consistency
        .forEach(cartItem -> signature.append(cartItem.getProduct().getId()).append(":")
            .append(cartItem.getQuantity()).append(";"));
    String hash = HashingUtil.sha256ToHex(signature.toString());
    return String.format("checkout:%s:%s", userId, hash);
  }

  private OrderEntity newOrder(CheckoutInput input, UserOutput user, List<CartItemEntity> cartItems) {
    OrderEntity order = new OrderEntity();
    order.setId(sequenceGenerator.generate(SequenceType.ORDER));
    order.setStatus(OrderStatus.AWAITING_STOCK_RESERVATION);

    // User
    order.setUser(OrderMapper.INSTANCE.toUserSnapshot(user));

    // Address
    order.setAddress(OrderMapper.INSTANCE.toAddress(input.getAddress()));

    for (CartItemEntity cartItem : cartItems) {
      ProductSnapshot product = OrderMapper.INSTANCE.toProductSnapshot(cartItem.getProduct());

      OrderItem orderItem = OrderItem.builder()
          .product(product)
          .quantity(cartItem.getQuantity())
          .build();
      order.getOrderItems().add(orderItem);
    }

    order.calculateOrderAmount();

    PaymentSnapshot payment = PaymentSnapshot.builder()
        .paymentGatewayId(input.getPaymentGatewayId())
        .build();
    order.setPayment(payment);

    return order;
  }
}
