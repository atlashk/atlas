package org.atlas.services.catalog.application.product.mapper;

import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;
import org.atlas.services.catalog.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductEventMapper {

  ProductEventMapper INSTANCE = Mappers.getMapper(ProductEventMapper.class);

  Product toProduct(ProductCreatedEvent event);

  void merge(Product product, @MappingTarget ProductCreatedEvent event);
}
