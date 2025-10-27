package org.atlas.domain.order.mapper;

import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderEntity.ProductSnapshot;
import org.atlas.domain.order.entity.OrderEntity.UserSnapshot;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.usecase.admin.model.AdminListOrderInput;
import org.atlas.domain.order.usecase.front.model.ListOrderInput;
import org.atlas.framework.internalapi.user.model.CartItemResponse.Product;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  UserSnapshot toUserSnapshot(UserResponse response);

  ProductSnapshot toProductSnapshot(Product product);

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "orderItems", source = "orderItems")
  @Mapping(target = "amount", source = "amount")
  CheckoutSagaData toCheckoutSagaData(OrderEntity entity);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "quantity", source = "quantity")
  CheckoutSagaData.OrderItem toCheckoutSagaDataOrderItem(OrderEntity.OrderItem orderItem);

  FindOrderCriteria toFindOrderCriteria(ListOrderInput input);

  FindOrderCriteria toFindOrderCriteria(AdminListOrderInput input);
}
