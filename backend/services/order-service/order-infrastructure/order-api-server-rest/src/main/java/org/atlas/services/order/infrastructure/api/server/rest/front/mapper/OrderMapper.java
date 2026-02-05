package org.atlas.services.order.infrastructure.api.server.rest.front.mapper;

import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.CheckoutRequest;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.OrderResponse;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.RetrieveOrderStatusResponse;
import org.atlas.services.order.port.in.front.model.CheckoutInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderStatusOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  CheckoutInput toCheckoutInput(CheckoutRequest request);

  RetrieveOrderStatusResponse toGetOrderStatusResponse(RetrieveOrderStatusOutput output);

  OrderResponse toOrderResponse(OrderEntity entity);

  OrderResponse.Address toAddressResponse(OrderEntity.Address address);

  OrderResponse.OrderItem toOrderItemResponse(OrderEntity.OrderItem orderItem);

  OrderResponse.Product toProductResponse(OrderEntity.ProductSnapshot product);

  @Mapping(target = "paymentGateway", source = "paymentGatewayName")
  OrderResponse.Payment toPaymentResponse(OrderEntity.PaymentSnapshot payment);
}
