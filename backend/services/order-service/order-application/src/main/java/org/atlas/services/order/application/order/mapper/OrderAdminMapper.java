package org.atlas.services.order.application.order.mapper;

import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.domain.entity.OrderEntity.Address;
import org.atlas.services.order.domain.entity.OrderEntity.OrderItem;
import org.atlas.services.order.domain.entity.OrderEntity.PaymentSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.ProductSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.UserSnapshot;
import org.atlas.services.order.port.in.order.model.admin.OrderOutput;
import org.atlas.services.order.port.in.order.model.admin.RetrieveOrderListInput;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderAdminMapper {

  OrderAdminMapper INSTANCE = Mappers.getMapper(OrderAdminMapper.class);

  OrderRepository.FindOrderCriteria toFindOrderCriteria(RetrieveOrderListInput input);

  OrderOutput toAdminOrderOutput(OrderEntity order);

  OrderOutput.User toAdminOrderOutputUser(UserSnapshot user);

  OrderOutput.Address toAdminOrderOutputAddress(Address address);

  OrderOutput.OrderItem toAdminOrderOutputOrderItem(OrderItem orderItem);

  OrderOutput.Product toAdminOrderOutputProduct(ProductSnapshot product);

  OrderOutput.Payment toAdminOrderOutputPayment(PaymentSnapshot payment);
}
