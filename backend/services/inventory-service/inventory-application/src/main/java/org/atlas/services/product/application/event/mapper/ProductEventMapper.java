package org.atlas.services.product.application.event.mapper;

import org.atlas.libs.framework.domain.common.event.contract.product.ProductCreatedEvent;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductEventMapper {

  ProductEventMapper INSTANCE = Mappers.getMapper(ProductEventMapper.class);

  StockEntity toProduct(ProductCreatedEvent event);

  void merge(StockEntity product, @MappingTarget ProductCreatedEvent event);
}
