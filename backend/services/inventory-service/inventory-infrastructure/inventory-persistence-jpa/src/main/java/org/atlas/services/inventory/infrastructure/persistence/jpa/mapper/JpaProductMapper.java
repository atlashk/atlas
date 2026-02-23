package org.atlas.services.inventory.infrastructure.persistence.jpa.mapper;

import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductAttributeEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductDetailsEntity;
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
  JpaProductEntity toJpaProduct(StockEntity product);

  /**
   * After mapping for {@link StockEntity} to {@link JpaProductEntity} - handles bidirectional relationships
   */
  @AfterMapping
  default void afterToJpaProduct(@MappingTarget JpaProductEntity jpaProduct, StockEntity product) {
    if (jpaProduct.getDetails() != null) {
      jpaProduct.getDetails().setProduct(jpaProduct);
    }

    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      product.getAttributes().forEach(attribute -> {
        JpaProductAttributeEntity jpaAttribute =
            JpaProductAttributeMapper.INSTANCE.toJpaProductAttribute(attribute);
        jpaProduct.addAttribute(jpaAttribute);
      });
    }
  }

  StockEntity toProduct(JpaProductEntity jpaProduct);

  @Mapping(target = "details", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  void merge(StockEntity product, @MappingTarget JpaProductEntity jpaProduct);

  /**
   * After mapping for merge operation - handles complex relationship updates
   */
  @AfterMapping
  default void afterMerge(@MappingTarget JpaProductEntity jpaProduct, StockEntity product) {
    if (product.getDetails() != null) {
      if (jpaProduct.getDetails() != null) {
        // Merge into existing details
        JpaProductDetailsMapper.INSTANCE.merge(product.getDetails(), jpaProduct.getDetails());
      } else {
        // Create new details if none exist
        JpaProductDetailsEntity jpaDetails =
            JpaProductDetailsMapper.INSTANCE.toJpaProductDetails(product.getDetails());
        jpaDetails.setProduct(jpaProduct);
        jpaProduct.setDetails(jpaDetails);
      }
    }

    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      // Clear existing attributes and add new ones using entity helper method
      jpaProduct.getAttributes().clear();
      product.getAttributes().forEach(attribute -> {
        JpaProductAttributeEntity jpaAttribute =
            JpaProductAttributeMapper.INSTANCE.toJpaProductAttribute(attribute);
        jpaProduct.addAttribute(jpaAttribute);
      });
    }
  }
}
