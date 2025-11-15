package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import org.atlas.domain.product.entity.ProductAttribute;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProductAttribute;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaProductAttributeMapper {

  JpaProductAttributeMapper INSTANCE = Mappers.getMapper(JpaProductAttributeMapper.class);

  @Mapping(target = "product", ignore = true)
  JpaProductAttribute toJpaProductAttribute(ProductAttribute productAttribute);

  @Mapping(target = "productId", source = "product.id")
  ProductAttribute toProductAttribute(JpaProductAttribute jpaProductAttribute);

  @Mapping(target = "product", ignore = true)
  void merge(ProductAttribute productAttribute,
      @MappingTarget JpaProductAttribute jpaProductAttribute);
}
