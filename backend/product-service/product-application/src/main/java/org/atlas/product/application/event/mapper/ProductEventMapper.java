package org.atlas.product.application.event.mapper;

import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.domain.common.event.contract.product.ProductEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductEventMapper {

  ProductEventMapper INSTANCE = Mappers.getMapper(ProductEventMapper.class);

  Product toProduct(ProductEvent event);

  @Mapping(source = "id", target = "productId")
  void merge(Product product, @MappingTarget ProductEvent event);
}
