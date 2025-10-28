package org.atlas.infrastructure.api.server.rest.impl.order.admin.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.infrastructure.api.server.rest.impl.order.admin.model.AdminOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  AdminOrderResponse toOrderResponse(Order order);
  AdminOrderResponse.User toUserResponse(Order.UserSnapshot userSnapshot);
  AdminOrderResponse.Address toAddressResponse(Order.Address address);
  AdminOrderResponse.OrderItem toOrderItemResponse(Order.OrderItem orderItem);
  AdminOrderResponse.Product toProductResponse(Order.ProductSnapshot productSnapshot);
  AdminOrderResponse.Payment toPaymentResponse(Order.PaymentSnapshot paymentSnapshot);
}
