package org.atlas.services.product.infrastructure.persistence.jpa.mapper;

import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.product.domain.entity.Product;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProduct;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductAttribute;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductDetails;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    uses = {
        JpaProductAttributeMapper.class,
        JpaProductDetailsMapper.class,
        JpaBrandMapper.class,
        JpaCategoryMapper.class
    },
    builder = @Builder(disableBuilder = true),
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface JpaProductMapper {

  JpaProductMapper INSTANCE = Mappers.getMapper(JpaProductMapper.class);

  @Mapping(target = "details", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  JpaProduct toJpaProduct(Product product);

  /**
   * After mapping for {@link Product} to {@link JpaProduct} - handles bidirectional relationships
   */
  @AfterMapping
  default void afterToJpaProduct(@MappingTarget JpaProduct jpaProduct, Product product) {
    if (jpaProduct.getDetails() != null) {
      jpaProduct.getDetails().setProduct(jpaProduct);
    }

    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      product.getAttributes().forEach(attribute -> {
        JpaProductAttribute jpaAttribute =
            JpaProductAttributeMapper.INSTANCE.toJpaProductAttribute(attribute);
        jpaProduct.addAttribute(jpaAttribute);
      });
    }
  }

  Product toProduct(JpaProduct jpaProduct);

  @Mapping(target = "details", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  void merge(Product product, @MappingTarget JpaProduct jpaProduct);

  /**
   * After mapping for merge operation - handles complex relationship updates
   */
  @AfterMapping
  default void afterMerge(@MappingTarget JpaProduct jpaProduct, Product product) {
    if (product.getDetails() != null) {
      if (jpaProduct.getDetails() != null) {
        // Merge into existing details
        JpaProductDetailsMapper.INSTANCE.merge(product.getDetails(), jpaProduct.getDetails());
      } else {
        // Create new details if none exist
        JpaProductDetails jpaDetails =
            JpaProductDetailsMapper.INSTANCE.toJpaProductDetails(product.getDetails());
        jpaDetails.setProduct(jpaProduct);
        jpaProduct.setDetails(jpaDetails);
      }
    }

    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      // Clear existing attributes and add new ones using entity helper method
      jpaProduct.getAttributes().clear();
      product.getAttributes().forEach(attribute -> {
        JpaProductAttribute jpaAttribute =
            JpaProductAttributeMapper.INSTANCE.toJpaProductAttribute(attribute);
        jpaProduct.addAttribute(jpaAttribute);
      });
    }
  }
}
