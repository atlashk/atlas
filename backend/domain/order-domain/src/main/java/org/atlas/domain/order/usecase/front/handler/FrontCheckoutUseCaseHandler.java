package org.atlas.domain.order.usecase.front.handler;

import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.domain.order.entity.PaymentEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.domain.order.mapper.OrderEventMapper;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.domain.order.usecase.front.model.FrontCheckoutInput;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.cryptography.HashingUtil;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.SequenceType;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class FrontCheckoutUseCaseHandler {

  private final OrderRepository orderRepository;
  private final MessagePublisherPort messagePublisherPort;
  private final LockPort lockPort;
  private final SequenceGenerator sequenceGenerator;

  public OrderEntity handle(FrontCheckoutInput input) {
    OrderEntity[] result = new OrderEntity[1];

    final Integer userId = Contexts.getUserId();

    // Payment idempotence guarantee
    String lockKey = obtainLockKey(input, userId);
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofMinutes(15);

    try {
      lockPort.doWithLock(() -> {
        // Insert order into DB
        OrderEntity orderEntity = newOrderEntity(input);
        orderRepository.insert(orderEntity);

        // Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(
            applicationConfigPort.getApplicationName(),
            OrderEventMapper.fromOrderEntity(orderEntity)
        );
        messagePublisherPort.publish(event);

        // Return the inserted order
        result[0] = orderEntity;
      }, lockKey, waitTime, leaseTime, true);
    } catch (LockAcquisitionException e) {
      log.warn("Duplicate order attempt detected: userId={}, input={}",
          userId, input, e);
      throw new DomainException(DomainError.CONFLICT, e);
    }

    return result[0];
  }

  private String obtainLockKey(FrontCheckoutInput input, Integer userId) {
    // Create a deterministic signature based on order items
    StringBuilder signature = new StringBuilder();
    input.getOrderItems().stream()
        .sorted(Comparator.comparingLong(
            FrontCheckoutInput.OrderItem::getProductId)) // Sort for consistency
        .forEach(item -> signature.append(item.getProductId())
            .append(":")
            .append(item.getQuantity())
            .append(";"));
    String hash = HashingUtil.sha256ToHex(signature.toString());
    return String.format("place-order:%d:%s", userId, hash);
  }

  private OrderEntity newOrderEntity(FrontCheckoutInput input) {
    // Order
    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setCode(sequenceGenerator.generate(SequenceType.ORDER));
    orderEntity.setStatus(OrderStatus.AWAITING_PRODUCT_RESERVATION);
    orderEntity.setCreatedAt(new Date());

    // User
    UserEntity userEntity = UserEntity.builder()
        .id(Contexts.getUserId())
        .build();
    orderEntity.setUser(userEntity);

    // Order Items
    for (FrontCheckoutInput.OrderItem orderItemInput : input.getOrderItems()) {
      OrderItemEntity orderItemEntity = new OrderItemEntity();
      orderItemEntity.setQuantity(orderItemInput.getQuantity());

      // Product
      ProductEntity productEntity = ProductEntity.builder()
          .id(orderItemInput.getProductId())
          .build();
      orderItemEntity.setProduct(productEntity);

      orderEntity.addOrderItem(orderItemEntity);
    }

    // Amount
    orderEntity.calculateOrderAmount();

    // Payment
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setMethod(input.getPaymentMethod());
    orderEntity.setPayment(paymentEntity);

    return orderEntity;
  }
}
