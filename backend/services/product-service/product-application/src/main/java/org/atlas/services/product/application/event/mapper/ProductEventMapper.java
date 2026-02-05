package org.atlas.services.product.application.event.mapper;

import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductEventMapper {

  ProductEventMapper INSTANCE = Mappers.getMapper(ProductEventMapper.class);

  ProductEntity toProduct(ProductEvent event);

  @Mapping(source = "productId", target = "productId")
  void merge(ProductEntity product, @MappingTarget ProductEvent event);
}
