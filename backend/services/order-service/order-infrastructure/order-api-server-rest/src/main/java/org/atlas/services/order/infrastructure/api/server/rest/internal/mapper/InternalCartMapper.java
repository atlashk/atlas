package org.atlas.services.order.infrastructure.api.server.rest.internal.mapper;

import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalCartMapper {

  InternalCartMapper INSTANCE = Mappers.getMapper(InternalCartMapper.class);

  CartResponse toCartResponse(CartEntity cart);

  // Don't remove it
  CartResponse.CartItem toCartItem(CartItemEntity cartItem);

  // Don't remove it
  CartResponse.Product toProduct(CartItemEntity.Product product);
}
