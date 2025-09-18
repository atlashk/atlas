package org.atlas.domain.order.usecase.front.handler;

import java.util.Collections;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.service.OrderAggregator;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.domain.order.usecase.front.model.FrontPlaceOrderInput;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.Product;
import org.atlas.framework.domain.event.contract.order.model.User;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.SequenceType;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class FrontPlaceOrderUseCaseHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;
  private final SequenceGenerator sequenceGenerator;

  public OrderEntity handle(FrontPlaceOrderInput input) {
    try {
      OrderEntity orderEntity = newOrder(input);
      orderEntity.setCode(sequenceGenerator.generate(SequenceType.ORDER));

      // Fetch user and products info from internal services
      orderAggregator.aggregate(Collections.singletonList(orderEntity), false);

      // Calculate order amount
      orderEntity.calculateOrderAmount();

      // Save into DB
      orderRepository.insert(orderEntity);

      // Publish event
      publishEvent(orderEntity);

      // Return the inserted order
      return orderEntity;
    } catch (Exception e) {
      log.error("Failed to place order", e);
      throw new DomainException(AppError.FAILED_TO_PLACE_ORDER);
    }
  }

  private OrderEntity newOrder(FrontPlaceOrderInput input) {
    // Order
    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setStatus(OrderStatus.PROCESSING);
    orderEntity.setCreatedAt(new Date());

    // User
    UserEntity userEntity = UserEntity.builder()
        .id(Contexts.getUserId())
        .build();
    orderEntity.setUser(userEntity);

    // Order Items
    for (FrontPlaceOrderInput.OrderItem orderItemInput : input.getOrderItems()) {
      OrderItemEntity orderItemEntity = new OrderItemEntity();
      orderItemEntity.setQuantity(orderItemInput.getQuantity());

      // Product
      ProductEntity productEntity = ProductEntity.builder()
          .id(orderItemInput.getProductId())
          .build();
      orderItemEntity.setProduct(productEntity);

      orderEntity.addOrderItem(orderItemEntity);
    }
    return orderEntity;
  }

  private void publishEvent(OrderEntity orderEntity) {
    OrderCreatedEvent event = new OrderCreatedEvent(applicationConfigPort.getApplicationName());

    // Map basic fields
    event.setOrderId(orderEntity.getId());
    event.setAmount(orderEntity.getAmount());
    event.setCreatedAt(orderEntity.getCreatedAt());

    // Map user
    if (orderEntity.getUser() != null) {
      event.setUser(ObjectMapperUtil.getInstance().map(orderEntity.getUser(), User.class));
    }

    // Map order items
    if (orderEntity.getOrderItems() != null) {
      for (OrderItemEntity orderItemEntity : orderEntity.getOrderItems()) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(
            ObjectMapperUtil.getInstance().map(orderItemEntity.getProduct(), Product.class));
        orderItem.setQuantity(orderItemEntity.getQuantity());
        event.addOrderItem(orderItem);
      }
    }

    messagePublisherPort.publish(event);
  }
}
