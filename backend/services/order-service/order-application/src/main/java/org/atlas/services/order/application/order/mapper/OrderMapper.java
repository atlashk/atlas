package org.atlas.services.order.application.order.mapper;

import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.services.order.domain.entity.CartItemEntity;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.domain.entity.OrderEntity.Address;
import org.atlas.services.order.domain.entity.OrderEntity.OrderItem;
import org.atlas.services.order.domain.entity.OrderEntity.ProductSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.UserSnapshot;
import org.atlas.services.order.port.in.order.model.CheckoutInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderListInput;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  OrderRepository.FindOrderCriteria toFindOrderCriteria(RetrieveOrderListInput input);

  UserSnapshot toUserSnapshot(UserOutput response);

  ProductSnapshot toProductSnapshot(CartItemEntity.Product product);

  Address toAddress(CheckoutInput.Address address);

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  CheckoutSagaData toCheckoutSagaData(OrderEntity entity);

  // Don't remove it
  CheckoutSagaData.User toCheckoutSagaDataUser(UserSnapshot userSnapshot);

  // Don't remove it
  CheckoutSagaData.Address toCheckoutSagaDataUser(Address address);

  // Don't remove it
  CheckoutSagaData.OrderItem toCheckoutSagaDataOrderItem(OrderItem orderItem);

  // Don't remove it
  CheckoutSagaData.Product toCheckoutSagaDataProduct(ProductSnapshot productSnapshot);
}
