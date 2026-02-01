package org.atlas.services.order.infrastructure.api.server.rest.admin.mapper;

import org.atlas.services.order.infrastructure.api.server.rest.admin.model.OrderResponse;
import org.atlas.services.order.domain.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  OrderResponse toOrderResponse(Order order);

  OrderResponse.User toUserResponse(Order.UserSnapshot userSnapshot);

  OrderResponse.Address toAddressResponse(Order.Address address);

  OrderResponse.OrderItem toOrderItemResponse(Order.OrderItem orderItem);

  OrderResponse.Product toProductResponse(Order.ProductSnapshot productSnapshot);

  @Mapping(target = "paymentGateway", source = "paymentGatewayName")
  OrderResponse.Payment toPaymentResponse(Order.PaymentSnapshot paymentSnapshot);
}
