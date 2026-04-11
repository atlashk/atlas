package org.atlas.services.order.application.order.mapper;

import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.security.Principal;
import org.atlas.services.order.domain.entity.CartItem;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.domain.entity.Order.Address;
import org.atlas.services.order.domain.entity.Order.OrderItem;
import org.atlas.services.order.domain.entity.Order.ProductSnapshot;
import org.atlas.services.order.domain.entity.Order.UserSnapshot;
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

  @Mapping(target = "id", source = "userId")
  UserSnapshot toUserSnapshot(Principal principal);

  ProductSnapshot toProductSnapshot(CartItem.Product product);

  Address toAddress(CheckoutInput.Address address);

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "paymentGatewayId", source = "payment.paymentGatewayId")
  CheckoutSagaData toCheckoutSagaData(Order entity);

  // Don't remove it
  CheckoutSagaData.User toCheckoutSagaDataUser(UserSnapshot userSnapshot);

  // Don't remove it
  CheckoutSagaData.Address toCheckoutSagaDataUser(Address address);

  // Don't remove it
  CheckoutSagaData.OrderItem toCheckoutSagaDataOrderItem(OrderItem orderItem);

  // Don't remove it
  CheckoutSagaData.Product toCheckoutSagaDataProduct(ProductSnapshot productSnapshot);
}
