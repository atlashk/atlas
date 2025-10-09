package org.atlas.infrastructure.persistence.jpa.impl.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.domain.order.entity.PaymentEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderItemEntity;

@UtilityClass
public class JpaOrderEntityMapper {

  public static JpaOrderEntity toJpaOrderEntity(final OrderEntity order) {
    // Order
    final JpaOrderEntity jpaOrderEntity = new JpaOrderEntity();
    jpaOrderEntity.setId(order.getId());
    jpaOrderEntity.setCode(order.getCode());
    jpaOrderEntity.setUserId(order.getUser().getId());
    jpaOrderEntity.setAmount(order.getAmount());
    jpaOrderEntity.setPaymentId(order.getPayment().getId());
    jpaOrderEntity.setPaymentMethod(order.getPayment().getMethod());
    jpaOrderEntity.setStatus(order.getStatus());
    jpaOrderEntity.setCancellationReason(order.getCancellationReason());
    jpaOrderEntity.setCreatedAt(order.getCreatedAt());

    // Order items
    order.getOrderItems().forEach(orderItemEntity -> {
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
    final OrderEntity order = new OrderEntity();
    order.setId(jpaOrderEntity.getId());
    order.setCode(jpaOrderEntity.getCode());
    order.setAmount(jpaOrderEntity.getAmount());
    order.setStatus(jpaOrderEntity.getStatus());
    order.setCancellationReason(jpaOrderEntity.getCancellationReason());
    order.setCreatedAt(jpaOrderEntity.getCreatedAt());

    // User
    UserEntity user = new UserEntity();
    user.setId(jpaOrderEntity.getUserId());
    order.setUser(user);

    // Order items
    if (jpaOrderEntity.getOrderItems() != null) {
      jpaOrderEntity.getOrderItems().forEach(jpaOrderItemEntity -> {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setId(jpaOrderItemEntity.getId());
        orderItemEntity.setOrderId(jpaOrderEntity.getId());
        orderItemEntity.setQuantity(jpaOrderItemEntity.getQuantity());

        // Product
        ProductEntity product = new ProductEntity();
        product.setId(jpaOrderItemEntity.getProductId());
        product.setPrice(jpaOrderItemEntity.getProductPrice());
        orderItemEntity.setProduct(product);

        order.addOrderItem(orderItemEntity);
      });
    }

    // Payment
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setId(jpaOrderEntity.getPaymentId());
    paymentEntity.setMethod(jpaOrderEntity.getPaymentMethod());
    order.setPayment(paymentEntity);

    return order;
  }
}
