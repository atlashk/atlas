package org.atlas.order.application.mapper;

import org.atlas.order.application.model.CheckoutInput;
import org.atlas.order.application.model.RetrieveOrderListInput;
import org.atlas.order.application.port.repository.criteria.FindOrderCriteria;
import org.atlas.order.domain.entity.Order;
import org.atlas.order.domain.entity.Order.Address;
import org.atlas.order.domain.entity.Order.OrderItem;
import org.atlas.order.domain.entity.Order.ProductSnapshot;
import org.atlas.order.domain.entity.Order.UserSnapshot;
import org.atlas.common.framework.internalapi.user.model.CartResponse;
import org.atlas.common.framework.internalapi.user.model.UserResponse;
import org.atlas.common.framework.saga.checkout.CheckoutSagaData;
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
