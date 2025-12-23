package org.atlas.infrastructure.persistence.jpa.adapter.product.mapper;

import org.atlas.domain.product.entity.ProductDetails;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaProductDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaProductDetailsMapper {

  JpaProductDetailsMapper INSTANCE = Mappers.getMapper(JpaProductDetailsMapper.class);

  @Mapping(target = "product", ignore = true)
  JpaProductDetails toJpaProductDetails(ProductDetails productDetails);

  ProductDetails toProductDetails(JpaProductDetails jpaProductDetails);

  @Mapping(target = "product", ignore = true)
  @Mapping(target = "productId", ignore = true)
  void merge(ProductDetails productDetails, @MappingTarget JpaProductDetails jpaProductDetails);
}
