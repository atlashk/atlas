package org.atlas.services.order.api.rest.order.mapper;

import org.atlas.services.order.api.rest.order.model.admin.OrderResponse;
import org.atlas.services.order.api.rest.order.model.admin.RetrieveOrderListRequest;
import org.atlas.services.order.port.in.order.model.admin.OrderOutput;
import org.atlas.services.order.port.in.order.model.admin.RetrieveOrderListInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderAdminMapper {

  OrderAdminMapper INSTANCE = Mappers.getMapper(OrderAdminMapper.class);

  // Request -> Input
  // -----------------------------------------------------------------------------------------------

  RetrieveOrderListInput toRetrieveOrderListInput(RetrieveOrderListRequest request);

  // Entity/Output -> Response
  // -----------------------------------------------------------------------------------------------

  OrderResponse toOrderResponse(OrderOutput order);

  // Don't remove it
  OrderResponse.User toOrderResponseUser(OrderOutput.User user);

  // Don't remove it
  OrderResponse.Address toOrderResponseAddress(OrderOutput.Address address);

  // Don't remove it
  OrderResponse.OrderItem toOrderResponseOrderItem(OrderOutput.OrderItem orderItem);

  // Don't remove it
  OrderResponse.Product toOrderResponseProduct(OrderOutput.Product product);

  // Don't remove it
  OrderResponse.Payment toOrderResponsePayment(OrderOutput.Payment payment);
}
