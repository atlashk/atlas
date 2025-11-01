package org.atlas.infrastructure.persistence.jpa.impl.order.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.OrderItem;
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
  @Mapping(target = "orderItems", source = "orderItems")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  JpaOrder toJpaOrder(Order order);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productPrice", source = "product.price")
  @Mapping(target = "order", ignore = true)
  JpaOrderItem toJpaOrderItem(OrderItem orderItem);

  @AfterMapping
  default void setOrderReference(@MappingTarget JpaOrder jpaOrder) {
    if (jpaOrder.getOrderItems() != null) {
      for (JpaOrderItem orderItem : jpaOrder.getOrderItems()) {
        orderItem.setOrder(jpaOrder);
      }
    }
  }

  @Mapping(target = "user.id", source = "userId")
  @Mapping(target = "address.street", source = "addressStreet")
  @Mapping(target = "address.city", source = "addressCity")
  @Mapping(target = "address.country", source = "addressCountry")
  @Mapping(target = "address.postalCode", source = "addressPostalCode")
  @Mapping(target = "payment.paymentGatewayId", source = "paymentGatewayId")
  Order toOrder(JpaOrder jpaOrder);

  @AfterMapping
  default void setInheritedFields(@MappingTarget Order order, JpaOrder jpaOrder) {
    order.setCreatedAt(jpaOrder.getCreatedAt());
    order.setUpdatedAt(jpaOrder.getUpdatedAt());
  }

  @Mapping(target = "product.id", source = "productId")
  @Mapping(target = "product.price", source = "productPrice")
  OrderItem toOrderItem(JpaOrderItem jpaOrderItem);
}
