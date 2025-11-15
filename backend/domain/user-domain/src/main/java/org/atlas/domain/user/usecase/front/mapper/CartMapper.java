package org.atlas.domain.user.usecase.front.mapper;

import org.atlas.domain.user.entity.CartItem;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  CartItem.Product toProduct(ProductResponse response);
}
