package org.atlas.infrastructure.api.server.rest.impl.order.front.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.domain.order.usecase.front.model.GetOrderStatusOutput;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.CheckoutRequest;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.GetOrderStatusResponse;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  @Mapping(target = "userId", ignore = true)
  CheckoutInput toCheckoutInput(CheckoutRequest request);

  GetOrderStatusResponse toGetOrderStatusResponse(GetOrderStatusOutput output);

  OrderResponse toOrderResponse(Order entity);

  OrderResponse.Address toAddressResponse(Order.Address address);

  OrderResponse.OrderItem toOrderItemResponse(Order.OrderItem orderItem);

  OrderResponse.Product toProductResponse(Order.ProductSnapshot product);

  @Mapping(target = "paymentGateway", source = "paymentGatewayName")
  OrderResponse.Payment toPaymentResponse(Order.PaymentSnapshot payment);
}
