package org.atlas.infrastructure.persistence.jpa.impl.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderEntity.ProductVO;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderItemEntity;

@UtilityClass
public class JpaOrderEntityMapper {

  public static JpaOrderEntity toJpaOrderEntity(final OrderEntity order) {
    // Order
    final JpaOrderEntity jpaOrder = new JpaOrderEntity();
    jpaOrder.setId(order.getId());
    jpaOrder.setSagaId(order.getSagaId());
    jpaOrder.setCode(order.getCode());
    jpaOrder.setUserId(order.getUser().getId());
    jpaOrder.setAmount(order.getAmount());
    jpaOrder.setPaymentMethod(order.getPayment().getMethod());
    jpaOrder.setStatus(order.getStatus());
    jpaOrder.setCancellationReason(order.getCancellationReason());
    jpaOrder.setCreatedAt(order.getCreatedAt());

    // Order items
    order.getOrderItems().forEach(orderItem -> {
      JpaOrderItemEntity jpaOrderItem = new JpaOrderItemEntity();
      jpaOrderItem.setProductId(orderItem.getProduct().getId());
      jpaOrderItem.setProductPrice(orderItem.getProduct().getPrice());
      jpaOrderItem.setQuantity(orderItem.getQuantity());
      jpaOrder.addOrderItem(jpaOrderItem);
    });

    return jpaOrder;
  }

  public static OrderEntity toOrderEntity(final JpaOrderEntity jpaOrder) {
    // Order
    final OrderEntity order = new OrderEntity();
    order.setId(jpaOrder.getId());
    order.setSagaId(jpaOrder.getSagaId());
    order.setCode(jpaOrder.getCode());
    order.setAmount(jpaOrder.getAmount());
    order.setStatus(jpaOrder.getStatus());
    order.setCancellationReason(jpaOrder.getCancellationReason());
    order.setCreatedAt(jpaOrder.getCreatedAt());

    // User
    UserVO user = new UserVO();
    user.setId(jpaOrder.getUserId());
    order.setUser(user);

    // Order items
    if (jpaOrder.getOrderItems() != null) {
      jpaOrder.getOrderItems().forEach(jpaOrderItem -> {
        OrderItemVO orderItem = new OrderItemVO();
        orderItem.setId(jpaOrderItem.getId());
        orderItem.setOrderId(jpaOrder.getId());
        orderItem.setQuantity(jpaOrderItem.getQuantity());

        // Product
        ProductVO product = new ProductVO();
        product.setId(jpaOrderItem.getProductId());
        product.setPrice(jpaOrderItem.getProductPrice());
        orderItem.setProduct(product);

        order.addOrderItem(orderItem);
      });
    }

    // Payment
    PaymentVO paymentVO = new PaymentVO();
    paymentVO.setMethod(jpaOrder.getPaymentMethod());
    order.setPayment(paymentVO);

    return order;
  }
}
