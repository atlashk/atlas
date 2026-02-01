package org.atlas.services.order.application.front.mapper;

import org.atlas.libs.framework.internalapi.catalog.model.ProductResponse;
import org.atlas.services.order.domain.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  CartItem.Product toProduct(ProductResponse response);
}
