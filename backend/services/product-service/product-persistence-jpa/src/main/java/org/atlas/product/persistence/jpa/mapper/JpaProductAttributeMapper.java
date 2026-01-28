package org.atlas.product.persistence.jpa.mapper;

import org.atlas.product.domain.entity.ProductAttribute;
import org.atlas.product.persistence.jpa.entity.JpaProductAttribute;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
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
