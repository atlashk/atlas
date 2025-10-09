package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductAttributeEntity;
import org.atlas.domain.product.entity.ProductDetailsEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaBrandEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaCategoryEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProductAttributeEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProductDetailsEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProductEntity;

@UtilityClass
public class JpaProductEntityMapper {

  public static JpaProductEntity toJpaProductEntity(ProductEntity product) {
    // Product
    JpaProductEntity jpaProductEntity = new JpaProductEntity();
    jpaProductEntity.setId(product.getId());
    jpaProductEntity.setName(product.getName());
    jpaProductEntity.setPrice(product.getPrice());
    jpaProductEntity.setQuantity(product.getQuantity());
    jpaProductEntity.setStatus(product.getStatus());
    jpaProductEntity.setAvailableFrom(product.getAvailableFrom());
    jpaProductEntity.setIsActive(product.getIsActive());

    // Set brand reference by ID only
    if (product.getBrand() != null && product.getBrand().getId() != null) {
      JpaBrandEntity jpaBrandEntity = new JpaBrandEntity();
      jpaBrandEntity.setId(product.getBrand().getId());
      jpaProductEntity.setBrand(jpaBrandEntity);
    }

    // Set details (with back-reference)
    if (product.getDetails() != null) {
      JpaProductDetailsEntity jpaProductDetailsEntity = jpaProductEntity.getDetails();
      if (jpaProductDetailsEntity == null) {
        jpaProductDetailsEntity = new JpaProductDetailsEntity();
        jpaProductDetailsEntity.setProduct(jpaProductEntity);
        jpaProductEntity.setDetails(jpaProductDetailsEntity);
      }
      jpaProductDetailsEntity.setDescription(product.getDetails().getDescription());
    }

    // Set attributes (with back-reference)
    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      for (ProductAttributeEntity productAttributeEntity : product.getAttributes()) {
        JpaProductAttributeEntity jpaProductAttributeEntity = new JpaProductAttributeEntity();
        jpaProductAttributeEntity.setId(productAttributeEntity.getId());
        jpaProductAttributeEntity.setName(productAttributeEntity.getName());
        jpaProductAttributeEntity.setValue(productAttributeEntity.getValue());
        jpaProductEntity.addAttribute(jpaProductAttributeEntity);
      }
    }

    // Set category references by ID only
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      for (CategoryEntity categoryEntity : product.getCategories()) {
        if (categoryEntity.getId() != null) {
          JpaCategoryEntity jpaCategoryEntity = new JpaCategoryEntity();
          jpaCategoryEntity.setId(categoryEntity.getId());
          jpaProductEntity.addCategory(jpaCategoryEntity);
        }
      }
    }

    return jpaProductEntity;
  }

  public static void merge(ProductEntity product, JpaProductEntity jpaProductEntity) {
    // Update basic fields
    jpaProductEntity.setId(product.getId());
    jpaProductEntity.setName(product.getName());
    jpaProductEntity.setPrice(product.getPrice());
    jpaProductEntity.setQuantity(product.getQuantity());
    jpaProductEntity.setStatus(product.getStatus());
    jpaProductEntity.setAvailableFrom(product.getAvailableFrom());
    jpaProductEntity.setIsActive(product.getIsActive());

    // Update brand reference
    if (product.getBrand() != null && product.getBrand().getId() != null) {
      JpaBrandEntity jpaBrandEntity = new JpaBrandEntity();
      jpaBrandEntity.setId(product.getBrand().getId());
      jpaProductEntity.setBrand(jpaBrandEntity);
    } else {
      jpaProductEntity.setBrand(null);
    }

    // Update details
    if (product.getDetails() != null) {
      JpaProductDetailsEntity jpaDetailsEntity = jpaProductEntity.getDetails();
      if (jpaDetailsEntity == null) {
        jpaDetailsEntity = new JpaProductDetailsEntity();
        jpaDetailsEntity.setProduct(jpaProductEntity);
        jpaProductEntity.setDetails(jpaDetailsEntity);
      }
      jpaDetailsEntity.setDescription(product.getDetails().getDescription());
    } else {
      jpaProductEntity.setDetails(null);
    }

    // Update attributes (update by name, add new, delete unmatched)
    List<JpaProductAttributeEntity> existingJpaAttributeEntities =
        jpaProductEntity.getAttributes() != null
            ? new ArrayList<>(jpaProductEntity.getAttributes())
            : new ArrayList<>();
    // Clear the attributes list to rebuild it
    jpaProductEntity.getAttributes().clear();
    // Process attributes from product
    if (CollectionUtil.isNotEmpty(product.getAttributes())) {
      for (ProductAttributeEntity attributeEntity : product.getAttributes()) {
        // Find existing attribute by name
        Optional<JpaProductAttributeEntity> existingJpaAttributeEntityOpt = existingJpaAttributeEntities.stream()
            .filter(jpaAttributeEntity ->
                jpaAttributeEntity.getName().equals(attributeEntity.getName()))
            .findFirst();
        if (existingJpaAttributeEntityOpt.isPresent()) {
          // Update existing attribute
          JpaProductAttributeEntity jpaAttributeEntity = existingJpaAttributeEntityOpt.get();
          jpaAttributeEntity.setValue(attributeEntity.getValue());
          jpaProductEntity.addAttribute(jpaAttributeEntity);
        } else {
          // Add new attribute
          JpaProductAttributeEntity jpaAttributeEntity = new JpaProductAttributeEntity();
          jpaAttributeEntity.setName(attributeEntity.getName());
          jpaAttributeEntity.setValue(attributeEntity.getValue());
          jpaProductEntity.addAttribute(jpaAttributeEntity);
        }
      }
    }

    // Update categories
    jpaProductEntity.getCategories().clear();
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      for (CategoryEntity categoryEntity : product.getCategories()) {
        if (categoryEntity.getId() != null) {
          JpaCategoryEntity jpaCategoryEntity = new JpaCategoryEntity();
          jpaCategoryEntity.setId(categoryEntity.getId());
          jpaProductEntity.addCategory(jpaCategoryEntity);
        }
      }
    }
  }

  public static ProductEntity toProductEntity(JpaProductEntity jpaProductEntity) {
    // Product
    ProductEntity product = new ProductEntity();
    product.setId(jpaProductEntity.getId());
    product.setName(jpaProductEntity.getName());
    product.setPrice(jpaProductEntity.getPrice());
    product.setQuantity(jpaProductEntity.getQuantity());
    product.setStatus(jpaProductEntity.getStatus());
    product.setAvailableFrom(jpaProductEntity.getAvailableFrom());
    product.setIsActive(jpaProductEntity.getIsActive());

    // Brand
    if (jpaProductEntity.getBrand() != null) {
      BrandEntity brandEntity = new BrandEntity();
      brandEntity.setId(jpaProductEntity.getBrand().getId());
      brandEntity.setName(jpaProductEntity.getBrand().getName());
      product.setBrand(brandEntity);
    }

    // Details
    if (jpaProductEntity.getDetails() != null) {
      ProductDetailsEntity productDetailsEntity = new ProductDetailsEntity();
      productDetailsEntity.setDescription(jpaProductEntity.getDetails().getDescription());
      product.setDetails(productDetailsEntity);
    }

    // Attributes
    if (CollectionUtil.isNotEmpty(jpaProductEntity.getAttributes())) {
      for (JpaProductAttributeEntity jpaProductAttributeEntity : jpaProductEntity.getAttributes()) {
        ProductAttributeEntity productAttributeEntity = new ProductAttributeEntity();
        productAttributeEntity.setId(jpaProductAttributeEntity.getId());
        productAttributeEntity.setName(jpaProductAttributeEntity.getName());
        productAttributeEntity.setValue(jpaProductAttributeEntity.getValue());
        product.addAttribute(productAttributeEntity);
      }
    }

    // Categories
    if (CollectionUtil.isNotEmpty(jpaProductEntity.getCategories())) {
      for (JpaCategoryEntity jpaCategoryEntity : jpaProductEntity.getCategories()) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(jpaCategoryEntity.getId());
        categoryEntity.setName(jpaCategoryEntity.getName());
        product.addCategory(categoryEntity);
      }
    }

    return product;
  }
}
