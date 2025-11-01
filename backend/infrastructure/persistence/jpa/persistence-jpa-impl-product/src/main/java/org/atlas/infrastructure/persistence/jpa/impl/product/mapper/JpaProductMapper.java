package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.entity.ProductAttribute;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProduct;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProductAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
    JpaProductAttributeMapper.class,
    JpaProductDetailsMapper.class,
    JpaBrandMapper.class,
    JpaCategoryMapper.class
})
public interface JpaProductMapper {

  JpaProductMapper INSTANCE = Mappers.getMapper(JpaProductMapper.class);

  JpaProduct toJpaProduct(Product product);

  default Set<JpaProductAttribute> mapAttributesToSet(List<ProductAttribute> attributes) {
    if (attributes == null) {
      return new HashSet<>();
    }
    Set<JpaProductAttribute> result = new HashSet<>();
    for (ProductAttribute attribute : attributes) {
      result.add(JpaProductAttributeMapper.INSTANCE.toJpaProductAttribute(attribute));
    }
    return result;
  }

  Product toProduct(JpaProduct jpaProduct);

  default List<ProductAttribute> mapAttributesToList(Set<JpaProductAttribute> attributes) {
    if (attributes == null) {
      return new ArrayList<>();
    }
    List<ProductAttribute> result = new ArrayList<>();
    for (JpaProductAttribute attribute : attributes) {
      ProductAttribute domainAttribute = JpaProductAttributeMapper.INSTANCE.toProductAttribute(
          attribute);
      result.add(domainAttribute);
    }
    return result;
  }

  void merge(Product product, @MappingTarget JpaProduct jpaProduct);
}
