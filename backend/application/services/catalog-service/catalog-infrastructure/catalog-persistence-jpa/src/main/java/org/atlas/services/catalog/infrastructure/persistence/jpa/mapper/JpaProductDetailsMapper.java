package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.catalog.domain.entity.ProductDetailsEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductDetailsEntity;
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
public interface JpaProductDetailsMapper {

  JpaProductDetailsMapper INSTANCE = Mappers.getMapper(JpaProductDetailsMapper.class);

  @Mapping(target = "product", ignore = true)
  JpaProductDetailsEntity toJpaProductDetails(ProductDetailsEntity productDetails);

  ProductDetailsEntity toProductDetails(JpaProductDetailsEntity jpaProductDetails);

  @Mapping(target = "product", ignore = true)
  @Mapping(target = "productId", ignore = true)
  void merge(ProductDetailsEntity productDetails, @MappingTarget JpaProductDetailsEntity jpaProductDetails);
}
