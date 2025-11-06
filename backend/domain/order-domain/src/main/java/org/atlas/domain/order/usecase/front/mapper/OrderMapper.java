package org.atlas.domain.order.usecase.front.mapper;

import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.Address;
import org.atlas.domain.order.entity.Order.OrderItem;
import org.atlas.domain.order.entity.Order.ProductSnapshot;
import org.atlas.domain.order.entity.Order.UserSnapshot;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.domain.order.usecase.front.model.ListOrderInput;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  UserSnapshot toUserSnapshot(UserResponse response);

  Address toAddress(CheckoutInput.Address address);

  ProductSnapshot toProductSnapshot(CartResponse.Product product);

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "orderCode", source = "code")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  CheckoutSagaData toCheckoutSagaData(Order entity);

  CheckoutSagaData.User toCheckoutSagaDataUser(UserSnapshot userSnapshot);

  CheckoutSagaData.Address toCheckoutSagaDataUser(Address address);

  CheckoutSagaData.OrderItem toCheckoutSagaDataOrderItem(OrderItem orderItem);

  CheckoutSagaData.Product toCheckoutSagaDataProduct(ProductSnapshot productSnapshot);

  FindOrderCriteria toFindOrderCriteria(ListOrderInput input);
}
