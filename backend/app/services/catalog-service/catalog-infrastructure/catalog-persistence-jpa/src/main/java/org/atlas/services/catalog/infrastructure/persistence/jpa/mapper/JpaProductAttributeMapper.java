package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.catalog.domain.entity.ProductAttribute;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductAttributeEntity;
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
  JpaProductAttributeEntity toJpaProductAttribute(ProductAttribute productAttribute);

  @Mapping(target = "productId", source = "product.id")
  ProductAttribute toProductAttribute(JpaProductAttributeEntity jpaProductAttribute);

  @Mapping(target = "product", ignore = true)
  void merge(ProductAttribute productAttribute,
      @MappingTarget JpaProductAttributeEntity jpaProductAttribute);
}
