package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import org.atlas.domain.product.entity.Product;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaProductMapper {

  JpaProductMapper INSTANCE = Mappers.getMapper(JpaProductMapper.class);

  JpaProduct toJpaProduct(Product product);

  Product toProduct(JpaProduct jpaProduct);

  void merge(Product product, @MappingTarget JpaProduct jpaProduct);
}
