package org.atlas.services.order.infrastructure.persistence.jpa.mapper;

import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.domain.entity.Order.OrderItem;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrder;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaOrderMapper {

  JpaOrderMapper INSTANCE = Mappers.getMapper(JpaOrderMapper.class);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userFirstName", source = "user.firstName")
  @Mapping(target = "userLastName", source = "user.lastName")
  @Mapping(target = "userEmail", source = "user.email")
  @Mapping(target = "userPhoneNumber", source = "user.phoneNumber")
  @Mapping(target = "addressStreet", source = "address.street")
  @Mapping(target = "addressCity", source = "address.city")
  @Mapping(target = "addressCountry", source = "address.country")
  @Mapping(target = "addressPostalCode", source = "address.postalCode")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  @Mapping(target = "paymentGatewayName", source = "payment.paymentGatewayName")
  @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
  @Mapping(target = "paymentMethodDetails", source = "payment.paymentMethodDetails")
  @Mapping(target = "paymentTransactionId", source = "payment.transactionId")
  @Mapping(target = "orderItems", ignore = true)
  JpaOrder toJpaOrder(Order order);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "productPrice", source = "product.price")
  @Mapping(target = "order", ignore = true)
  JpaOrderItem toJpaOrderItem(OrderItem orderItem);

  /**
   * After mapping for {@link Order} to {@link JpaOrder} - handles bidirectional relationships
   */
  @AfterMapping
  default void afterToJpaOrder(@MappingTarget JpaOrder jpaOrder, Order order) {
    if (CollectionUtil.isNotEmpty(order.getOrderItems())) {
      order.getOrderItems().forEach(orderItem -> {
        JpaOrderItem jpaOrderItem = toJpaOrderItem(orderItem);
        jpaOrder.addOrderItem(jpaOrderItem);
      });
    }
  }

  @Mapping(target = "user.id", source = "userId")
  @Mapping(target = "user.firstName", source = "userFirstName")
  @Mapping(target = "user.lastName", source = "userLastName")
  @Mapping(target = "user.email", source = "userEmail")
  @Mapping(target = "user.phoneNumber", source = "userPhoneNumber")
  @Mapping(target = "address.street", source = "addressStreet")
  @Mapping(target = "address.city", source = "addressCity")
  @Mapping(target = "address.country", source = "addressCountry")
  @Mapping(target = "address.postalCode", source = "addressPostalCode")
  @Mapping(target = "payment.paymentGatewayId", source = "paymentGatewayId")
  @Mapping(target = "payment.paymentGatewayName", source = "paymentGatewayName")
  @Mapping(target = "payment.paymentMethod", source = "paymentMethod")
  @Mapping(target = "payment.paymentMethodDetails", source = "paymentMethodDetails")
  @Mapping(target = "payment.transactionId", source = "paymentTransactionId")
  Order toOrder(JpaOrder jpaOrder);

  @Mapping(target = "product.id", source = "productId")
  @Mapping(target = "product.name", source = "productName")
  @Mapping(target = "product.price", source = "productPrice")
  OrderItem toOrderItem(JpaOrderItem jpaOrderItem);
}
