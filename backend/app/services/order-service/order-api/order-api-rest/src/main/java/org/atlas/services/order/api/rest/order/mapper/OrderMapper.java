package org.atlas.services.order.api.rest.order.mapper;

import org.atlas.services.order.api.rest.order.model.CheckoutRequest;
import org.atlas.services.order.api.rest.order.model.OrderResponse;
import org.atlas.services.order.api.rest.order.model.RetrieveOrderListRequest;
import org.atlas.services.order.api.rest.order.model.RetrieveOrderStatusResponse;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.port.in.order.model.CheckoutInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderListInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderStatusOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  // Request -> Input
  // -----------------------------------------------------------------------------------------------

  CheckoutInput toCheckoutInput(CheckoutRequest request);

  RetrieveOrderListInput toRetrieveOrderListInput(RetrieveOrderListRequest request);

  // Entity/Output -> Response
  // -----------------------------------------------------------------------------------------------

  RetrieveOrderStatusResponse toRetrieveOrderStatusResponse(RetrieveOrderStatusOutput output);

  OrderResponse toOrderResponse(Order entity);

  // Don't remove it
  OrderResponse.Address toOrderResponseAddress(Order.Address address);

  // Don't remove it
  OrderResponse.OrderItem toOrderResponseOrderItem(Order.OrderItem orderItem);

  // Don't remove it
  OrderResponse.Product toOrderResponseProduct(Order.ProductSnapshot productSnapshot);

  // Don't remove it
  OrderResponse.Payment toOrderResponsePayment(Order.PaymentSnapshot paymentSnapshot);
}
