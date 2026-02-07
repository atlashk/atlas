package org.atlas.services.order.application.admin.mapper;

import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.domain.entity.OrderEntity.Address;
import org.atlas.services.order.domain.entity.OrderEntity.OrderItem;
import org.atlas.services.order.domain.entity.OrderEntity.PaymentSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.ProductSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.UserSnapshot;
import org.atlas.services.order.port.in.admin.model.AdminOrderOutput;
import org.atlas.services.order.port.in.admin.model.AdminRetrieveOrderListInput;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  OrderRepository.FindOrderCriteria toFindOrderCriteria(AdminRetrieveOrderListInput input);

  AdminOrderOutput toAdminOrderOutput(OrderEntity order);

  AdminOrderOutput.User toAdminOrderOutputUser(UserSnapshot user);

  AdminOrderOutput.Address toAdminOrderOutputAddress(Address address);

  AdminOrderOutput.OrderItem toAdminOrderOutputOrderItem(OrderItem orderItem);

  AdminOrderOutput.Product toAdminOrderOutputProduct(ProductSnapshot product);

  AdminOrderOutput.Payment toAdminOrderOutputPayment(PaymentSnapshot payment);
}
