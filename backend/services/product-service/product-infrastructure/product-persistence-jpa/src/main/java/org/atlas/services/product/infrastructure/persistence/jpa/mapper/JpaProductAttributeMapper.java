package org.atlas.services.product.infrastructure.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.ProductAttributeEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductAttribute;
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
  JpaProductAttribute toJpaProductAttribute(ProductAttributeEntity productAttribute);

  @Mapping(target = "productId", source = "product.id")
  ProductAttributeEntity toProductAttribute(JpaProductAttribute jpaProductAttribute);

  @Mapping(target = "product", ignore = true)
  void merge(ProductAttributeEntity productAttribute,
      @MappingTarget JpaProductAttribute jpaProductAttribute);
}
