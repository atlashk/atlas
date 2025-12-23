package org.atlas.infrastructure.api.server.rest.adapter.order.front.mapper;

import org.atlas.application.order.model.CheckoutInput;
import org.atlas.application.order.model.RetrieveOrderStatusOutput;
import org.atlas.domain.order.entity.Order;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.CheckoutRequest;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.OrderResponse;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.RetrieveOrderStatusResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  @Mapping(target = "userId", ignore = true)
  CheckoutInput toCheckoutInput(CheckoutRequest request);

  RetrieveOrderStatusResponse toGetOrderStatusResponse(RetrieveOrderStatusOutput output);

  OrderResponse toOrderResponse(Order entity);

  OrderResponse.Address toAddressResponse(Order.Address address);

  OrderResponse.OrderItem toOrderItemResponse(Order.OrderItem orderItem);

  OrderResponse.Product toProductResponse(Order.ProductSnapshot product);

  @Mapping(target = "paymentGateway", source = "paymentGatewayName")
  OrderResponse.Payment toPaymentResponse(Order.PaymentSnapshot payment);
}
