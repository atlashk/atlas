package org.atlas.services.order.infrastructure.api.server.rest.admin.mapper;

import org.atlas.services.order.infrastructure.api.server.rest.admin.model.AdminOrderResponse;
import org.atlas.services.order.infrastructure.api.server.rest.admin.model.AdminRetrieveOrderListRequest;
import org.atlas.services.order.port.in.admin.model.AdminOrderOutput;
import org.atlas.services.order.port.in.admin.model.AdminRetrieveOrderListInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  AdminRetrieveOrderListInput toAdminRetrieveOrderListInput(AdminRetrieveOrderListRequest request);

  AdminOrderResponse toAdminOrderResponse(AdminOrderOutput order);

  AdminOrderResponse.User toAdminOrderResponseUser(AdminOrderOutput.User user);

  AdminOrderResponse.Address toAdminOrderResponseAddress(AdminOrderOutput.Address address);

  AdminOrderResponse.OrderItem toAdminOrderResponseOrderItem(AdminOrderOutput.OrderItem orderItem);

  AdminOrderResponse.Product toAdminOrderResponseProduct(AdminOrderOutput.Product product);

  AdminOrderResponse.Payment toAdminOrderResponsePayment(AdminOrderOutput.Payment payment);
}
