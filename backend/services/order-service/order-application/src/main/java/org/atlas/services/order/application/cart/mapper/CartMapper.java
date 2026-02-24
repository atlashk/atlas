package org.atlas.services.order.application.cart.mapper;

import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.services.order.domain.entity.CartEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  CartEntity.Product toProduct(ProductOutput output);
}
