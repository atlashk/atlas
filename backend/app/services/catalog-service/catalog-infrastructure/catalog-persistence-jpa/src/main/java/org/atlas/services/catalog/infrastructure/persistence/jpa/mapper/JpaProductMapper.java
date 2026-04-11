package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductAttributeEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductDetailsEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductEntity;
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

  Product toProduct(JpaProductEntity jpaProduct);

  @Mapping(target = "details", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  JpaProductEntity toJpaProduct(Product product);

  /**
   * After mapping for {@link Product} to {@link JpaProductEntity} - handles bidirectional relationships
   */
  @AfterMapping
  default void afterToJpaProduct(@MappingTarget JpaProductEntity jpaProduct, Product product) {
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

  @Mapping(target = "details", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  void merge(Product product, @MappingTarget JpaProductEntity jpaProduct);

  /**
   * After mapping for merge operation - handles complex relationship updates
   */
  @AfterMapping
  default void afterMerge(@MappingTarget JpaProductEntity jpaProduct, Product product) {
    // Merge product details
    if (product.getDetails() != null) {
      if (jpaProduct.getDetails() != null) {
        JpaProductDetailsMapper.INSTANCE.merge(product.getDetails(), jpaProduct.getDetails());
      } else {
        JpaProductDetailsEntity jpaDetails =
            JpaProductDetailsMapper.INSTANCE.toJpaProductDetails(product.getDetails());
        jpaDetails.setProduct(jpaProduct);
        jpaProduct.setDetails(jpaDetails);
      }
    }

    // Merge product attributes
    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      Map<Integer, JpaProductAttributeEntity> existingById = new HashMap<>();
      if (CollectionUtil.isNotEmpty(jpaProduct.getAttributes())) {
        jpaProduct.getAttributes().forEach(existingAttribute -> {
          if (existingAttribute.getId() != null) {
            existingById.put(existingAttribute.getId(), existingAttribute);
          }
        });
      }

      Set<JpaProductAttributeEntity> merged = new HashSet<>();
      product.getAttributes().forEach(attribute -> {
        Integer attributeId = attribute.getId();
        if (attributeId != null) {
          JpaProductAttributeEntity existingAttribute = existingById.get(attributeId);
          if (existingAttribute != null) {
            JpaProductAttributeMapper.INSTANCE.merge(attribute, existingAttribute);
            merged.add(existingAttribute);
            return;
          }
        }

        JpaProductAttributeEntity jpaAttribute = JpaProductAttributeMapper.INSTANCE
            .toJpaProductAttribute(attribute);
        if (attributeId != null) {
          jpaAttribute.setId(null);
        }
        jpaProduct.addAttribute(jpaAttribute);
        merged.add(jpaAttribute);
      });

      jpaProduct.getAttributes()
          .removeIf(existingAttribute -> !merged.contains(existingAttribute));
    }
  }
}
