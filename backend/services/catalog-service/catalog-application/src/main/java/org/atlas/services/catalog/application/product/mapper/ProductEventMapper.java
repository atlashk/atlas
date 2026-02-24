package org.atlas.services.catalog.application.product.mapper;

import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductEventMapper {

  ProductEventMapper INSTANCE = Mappers.getMapper(ProductEventMapper.class);

  ProductEntity toProduct(ProductCreatedEvent event);

  void merge(ProductEntity product, @MappingTarget ProductCreatedEvent event);
}
