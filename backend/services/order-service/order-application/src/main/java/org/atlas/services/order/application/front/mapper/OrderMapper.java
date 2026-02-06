package org.atlas.services.order.application.front.mapper;

import org.atlas.libs.framework.internalapi.iam.model.UserResponse;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.domain.entity.OrderEntity.Address;
import org.atlas.services.order.domain.entity.OrderEntity.OrderItem;
import org.atlas.services.order.domain.entity.OrderEntity.ProductSnapshot;
import org.atlas.services.order.domain.entity.OrderEntity.UserSnapshot;
import org.atlas.services.order.port.in.front.model.CheckoutInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderListInput;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  OrderRepository.FindOrderCriteria toFindOrderCriteria(RetrieveOrderListInput input);

  UserSnapshot toUserSnapshot(UserResponse response);

  ProductSnapshot toProductSnapshot(CartEntity.Product product);

  Address toAddress(CheckoutInput.Address address);

  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  CheckoutSagaData toCheckoutSagaData(OrderEntity entity);

  CheckoutSagaData.User toCheckoutSagaDataUser(UserSnapshot userSnapshot);

  CheckoutSagaData.Address toCheckoutSagaDataUser(Address address);

  CheckoutSagaData.OrderItem toCheckoutSagaDataOrderItem(OrderItem orderItem);

  CheckoutSagaData.Product toCheckoutSagaDataProduct(ProductSnapshot productSnapshot);
}
