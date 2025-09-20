package org.atlas.infrastructure.persistence.jpa.impl.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderItemEntity;

@UtilityClass
public class JpaOrderEntityMapper {

  public static JpaOrderEntity toJpaOrderEntity(final OrderEntity orderEntity) {
    // Order
    final JpaOrderEntity jpaOrderEntity = new JpaOrderEntity();
    jpaOrderEntity.setId(orderEntity.getId());
    jpaOrderEntity.setCode(orderEntity.getCode());
    jpaOrderEntity.setUserId(orderEntity.getUser().getId());
    jpaOrderEntity.setAmount(orderEntity.getAmount());
    jpaOrderEntity.setStatus(orderEntity.getStatus());
    jpaOrderEntity.setCancellationReason(orderEntity.getCancellationReason());
    jpaOrderEntity.setCreatedAt(orderEntity.getCreatedAt());

    // Order items
    orderEntity.getOrderItems().forEach(orderItemEntity -> {
      JpaOrderItemEntity jpaOrderItemEntity = new JpaOrderItemEntity();
      jpaOrderItemEntity.setProductId(orderItemEntity.getProduct().getId());
      jpaOrderItemEntity.setProductPrice(orderItemEntity.getProduct().getPrice());
      jpaOrderItemEntity.setQuantity(orderItemEntity.getQuantity());
      jpaOrderEntity.addOrderItem(jpaOrderItemEntity);
    });

    return jpaOrderEntity;
  }

  public static OrderEntity toOrderEntity(final JpaOrderEntity jpaOrderEntity) {
    // Order
    final OrderEntity orderEntity = new OrderEntity();
    orderEntity.setId(jpaOrderEntity.getId());
    orderEntity.setCode(jpaOrderEntity.getCode());
    orderEntity.setAmount(jpaOrderEntity.getAmount());
    orderEntity.setStatus(jpaOrderEntity.getStatus());
    orderEntity.setCancellationReason(jpaOrderEntity.getCancellationReason());
    orderEntity.setCreatedAt(jpaOrderEntity.getCreatedAt());

    // User
    UserEntity userEntity = new UserEntity();
    userEntity.setId(jpaOrderEntity.getUserId());
    orderEntity.setUser(userEntity);

    // Order items
    if (jpaOrderEntity.getOrderItems() != null) {
      jpaOrderEntity.getOrderItems().forEach(jpaOrderItemEntity -> {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setId(jpaOrderItemEntity.getId());
        orderItemEntity.setOrderId(jpaOrderEntity.getId());
        orderItemEntity.setQuantity(jpaOrderItemEntity.getQuantity());

        // Product
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(jpaOrderItemEntity.getProductId());
        productEntity.setPrice(jpaOrderItemEntity.getProductPrice());
        orderItemEntity.setProduct(productEntity);

        orderEntity.addOrderItem(orderItemEntity);
      });
    }

    return orderEntity;
  }
}
