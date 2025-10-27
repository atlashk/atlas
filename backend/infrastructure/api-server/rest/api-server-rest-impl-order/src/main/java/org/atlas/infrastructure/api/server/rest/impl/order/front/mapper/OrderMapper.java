package org.atlas.infrastructure.api.server.rest.impl.order.front.mapper;

import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.domain.order.usecase.front.model.GetOrderStatusOutput;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.CheckoutRequest;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.GetOrderStatusResponse;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  CheckoutInput toCheckoutInput(CheckoutRequest request);

  GetOrderStatusResponse toGetOrderStatusResponse(GetOrderStatusOutput output);

  OrderResponse toOrderResponse(OrderEntity entity);
  OrderResponse.Address toAddressResponse(OrderEntity.Address address);
  OrderResponse.OrderItem toOrderItemResponse(OrderEntity.OrderItem orderItem);
  OrderResponse.Product toProductResponse(OrderEntity.ProductSnapshot product);
  OrderResponse.Payment toPaymentResponse(OrderEntity.PaymentSnapshot payment);
}
