package org.atlas.services.order.api.rest.order.mapper;

import org.atlas.services.order.api.rest.order.model.admin.OrderResponse;
import org.atlas.services.order.api.rest.order.model.admin.RetrieveOrderListRequest;
import org.atlas.services.order.domain.entity.OrderEntity;
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

  OrderResponse toOrderResponse(OrderEntity entity);

  // Don't remove it
  OrderResponse.User toOrderResponseAddress(OrderEntity.UserSnapshot userSnapshot);

  // Don't remove it
  OrderResponse.Address toOrderResponseAddress(OrderEntity.Address address);

  // Don't remove it
  OrderResponse.OrderItem toOrderResponseOrderItem(OrderEntity.OrderItem orderItem);

  // Don't remove it
  OrderResponse.Product toOrderResponseProduct(OrderEntity.ProductSnapshot productSnapshot);

  // Don't remove it
  OrderResponse.Payment toOrderResponsePayment(OrderEntity.PaymentSnapshot paymentSnapshot);
}
