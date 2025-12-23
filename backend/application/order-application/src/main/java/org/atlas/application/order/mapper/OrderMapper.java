package org.atlas.application.order.mapper;

import org.atlas.application.order.model.CheckoutInput;
import org.atlas.application.order.model.RetrieveOrderListInput;
import org.atlas.application.order.port.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.Address;
import org.atlas.domain.order.entity.Order.OrderItem;
import org.atlas.domain.order.entity.Order.ProductSnapshot;
import org.atlas.domain.order.entity.Order.UserSnapshot;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  FindOrderCriteria toFindOrderCriteria(RetrieveOrderListInput input);

  UserSnapshot toUserSnapshot(UserResponse response);

  ProductSnapshot toProductSnapshot(CartResponse.Product product);

  Address toAddress(CheckoutInput.Address address);

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "orderCode", source = "code")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  CheckoutSagaData toCheckoutSagaData(Order entity);

  CheckoutSagaData.User toCheckoutSagaDataUser(UserSnapshot userSnapshot);

  CheckoutSagaData.Address toCheckoutSagaDataUser(Address address);

  CheckoutSagaData.OrderItem toCheckoutSagaDataOrderItem(OrderItem orderItem);

  CheckoutSagaData.Product toCheckoutSagaDataProduct(ProductSnapshot productSnapshot);
}
