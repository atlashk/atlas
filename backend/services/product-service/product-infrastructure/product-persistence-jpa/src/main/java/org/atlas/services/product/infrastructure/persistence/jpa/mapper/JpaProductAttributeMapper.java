package org.atlas.services.product.infrastructure.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.ProductAttributeEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductAttributeEntity;
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
  JpaProductAttributeEntity toJpaProductAttribute(ProductAttributeEntity productAttribute);

  @Mapping(target = "productId", source = "product.productId")
  ProductAttributeEntity toProductAttribute(JpaProductAttributeEntity jpaProductAttribute);

  @Mapping(target = "product", ignore = true)
  void merge(ProductAttributeEntity productAttribute,
      @MappingTarget JpaProductAttributeEntity jpaProductAttribute);
}
