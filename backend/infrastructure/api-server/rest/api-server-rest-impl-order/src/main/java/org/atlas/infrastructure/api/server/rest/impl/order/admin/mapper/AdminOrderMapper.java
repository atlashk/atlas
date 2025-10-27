package org.atlas.infrastructure.api.server.rest.impl.order.admin.mapper;

import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.domain.order.usecase.front.model.GetOrderStatusOutput;
import org.atlas.infrastructure.api.server.rest.impl.order.admin.model.AdminOrderResponse;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.CheckoutRequest;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.GetOrderStatusResponse;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  AdminOrderResponse toOrderResponse(OrderEntity entity);
  AdminOrderResponse.User toUserResponse(OrderEntity.UserSnapshot user);
  AdminOrderResponse.Address toAddressResponse(OrderEntity.Address address);
  AdminOrderResponse.OrderItem toOrderItemResponse(OrderEntity.OrderItem orderItem);
  AdminOrderResponse.Product toProductResponse(OrderEntity.ProductSnapshot product);
  AdminOrderResponse.Payment toPaymentResponse(OrderEntity.PaymentSnapshot payment);
}
