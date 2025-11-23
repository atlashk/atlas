package org.atlas.domain.product.event.mapper;

import org.atlas.domain.product.entity.Product;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
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
