package org.atlas.infrastructure.persistence.jpa.impl.order.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.OrderItem;
import org.atlas.domain.order.entity.Order.PaymentSnapshot;
import org.atlas.domain.order.entity.Order.ProductSnapshot;
import org.atlas.domain.order.entity.Order.UserSnapshot;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrder;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
  JpaOrderItem toJpaOrderItem(OrderItem orderItem);

  @Mapping(target = "user.id", source = "userId")
  @Mapping(target = "address.street", source = "addressStreet")
  @Mapping(target = "address.city", source = "addressCity")
  @Mapping(target = "address.country", source = "addressCountry")
  @Mapping(target = "address.postalCode", source = "addressPostalCode")
  Order toOrder(JpaOrder jpaOrder);

  OrderItem toOrderItem(JpaOrderItem jpaOrderItem);

  @Mapping(target = "id", source = "productId")
  @Mapping(target = "price", source = "productPrice")
  ProductSnapshot toProductSnapshot(JpaOrderItem jpaOrderItem);

  PaymentSnapshot toPaymentSnapshot(String paymentMethod);
}
