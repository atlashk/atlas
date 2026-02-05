package org.atlas.services.order.infrastructure.api.server.rest.admin.mapper;

import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.infrastructure.api.server.rest.admin.model.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  OrderResponse toOrderResponse(OrderEntity order);

  OrderResponse.User toUserResponse(OrderEntity.UserSnapshot userSnapshot);

  OrderResponse.Address toAddressResponse(OrderEntity.Address address);

  OrderResponse.OrderItem toOrderItemResponse(OrderEntity.OrderItem orderItem);

  OrderResponse.Product toProductResponse(OrderEntity.ProductSnapshot productSnapshot);

  @Mapping(target = "paymentGateway", source = "paymentGatewayName")
  OrderResponse.Payment toPaymentResponse(OrderEntity.PaymentSnapshot paymentSnapshot);
}
