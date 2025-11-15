package org.atlas.infrastructure.api.server.rest.impl.order.admin.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.infrastructure.api.server.rest.impl.order.admin.model.AdminOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  AdminOrderResponse toOrderResponse(Order order);

  AdminOrderResponse.User toUserResponse(Order.UserSnapshot userSnapshot);

  AdminOrderResponse.Address toAddressResponse(Order.Address address);

  AdminOrderResponse.OrderItem toOrderItemResponse(Order.OrderItem orderItem);

  AdminOrderResponse.Product toProductResponse(Order.ProductSnapshot productSnapshot);

  @Mapping(target = "paymentGateway", source = "paymentGatewayName")
  AdminOrderResponse.Payment toPaymentResponse(Order.PaymentSnapshot paymentSnapshot);
}
