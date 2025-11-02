package org.atlas.infrastructure.persistence.jpa.impl.order.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.OrderItem;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrder;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaOrderMapper {

  JpaOrderMapper INSTANCE = Mappers.getMapper(JpaOrderMapper.class);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "addressStreet", source = "address.street")
  @Mapping(target = "addressCity", source = "address.city")
  @Mapping(target = "addressCountry", source = "address.country")
  @Mapping(target = "addressPostalCode", source = "address.postalCode")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  @Mapping(target = "paymentGatewayName", source = "payment.paymentGatewayName")
  @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
  @Mapping(target = "paymentMethodDetails", source = "payment.paymentMethodDetails")
  @Mapping(target = "orderItems", ignore = true)
  JpaOrder toJpaOrder(Order order);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "productPrice", source = "product.price")
  @Mapping(target = "order", ignore = true)
  JpaOrderItem toJpaOrderItem(OrderItem orderItem);

  @AfterMapping
  default void afterToJpaOrder(@MappingTarget JpaOrder jpaOrder, Order order) {
    if (CollectionUtil.isNotEmpty(order.getOrderItems())) {
      order.getOrderItems().forEach(orderItem -> {
        JpaOrderItem jpaOrderItem = toJpaOrderItem(orderItem);
        // Bidirectional handling
        jpaOrder.addOrderItem(jpaOrderItem);
      });
    }
  }

  @Mapping(target = "user.id", source = "userId")
  @Mapping(target = "address.street", source = "addressStreet")
  @Mapping(target = "address.city", source = "addressCity")
  @Mapping(target = "address.country", source = "addressCountry")
  @Mapping(target = "address.postalCode", source = "addressPostalCode")
  @Mapping(target = "payment.paymentGatewayId", source = "paymentGatewayId")
  @Mapping(target = "payment.paymentGatewayName", source = "paymentGatewayName")
  @Mapping(target = "payment.paymentMethod", source = "paymentMethod")
  @Mapping(target = "payment.paymentMethodDetails", source = "paymentMethodDetails")
  Order toOrder(JpaOrder jpaOrder);

  @Mapping(target = "product.id", source = "productId")
  @Mapping(target = "product.name", source = "productName")
  @Mapping(target = "product.price", source = "productPrice")
  OrderItem toOrderItem(JpaOrderItem jpaOrderItem);

  @AfterMapping
  default void setInheritedFields(@MappingTarget Order order, JpaOrder jpaOrder) {
    order.setCreatedAt(jpaOrder.getCreatedAt());
    order.setUpdatedAt(jpaOrder.getUpdatedAt());
  }
}
