package org.atlas.product.persistence.jpa.mapper;

import org.atlas.product.domain.entity.ProductDetails;
import org.atlas.product.persistence.jpa.entity.JpaProductDetails;
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
  JpaProductDetails toJpaProductDetails(ProductDetails productDetails);

  ProductDetails toProductDetails(JpaProductDetails jpaProductDetails);

  @Mapping(target = "product", ignore = true)
  @Mapping(target = "productId", ignore = true)
  void merge(ProductDetails productDetails, @MappingTarget JpaProductDetails jpaProductDetails);
}
