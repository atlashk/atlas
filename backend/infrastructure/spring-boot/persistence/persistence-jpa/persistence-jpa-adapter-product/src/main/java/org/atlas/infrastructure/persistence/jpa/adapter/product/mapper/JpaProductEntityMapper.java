package org.atlas.infrastructure.persistence.jpa.adapter.product.mapper;

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
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaBrandEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaCategoryEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaProductAttributeEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaProductDetailsEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaProductEntity;

@UtilityClass
public class JpaProductEntityMapper {

  public static JpaProductEntity toJpaProductEntity(ProductEntity productEntity) {
    // Product
    JpaProductEntity jpaProductEntity = new JpaProductEntity();
    jpaProductEntity.setId(productEntity.getId());
    jpaProductEntity.setName(productEntity.getName());
    jpaProductEntity.setPrice(productEntity.getPrice());
    jpaProductEntity.setQuantity(productEntity.getQuantity());
    jpaProductEntity.setStatus(productEntity.getStatus());
    jpaProductEntity.setAvailableFrom(productEntity.getAvailableFrom());
    jpaProductEntity.setIsActive(productEntity.getIsActive());

    // Set brand reference by ID only
    if (productEntity.getBrand() != null && productEntity.getBrand().getId() != null) {
      JpaBrandEntity jpaBrandEntity = new JpaBrandEntity();
      jpaBrandEntity.setId(productEntity.getBrand().getId());
      jpaProductEntity.setBrand(jpaBrandEntity);
    }

    // Set details (with back-reference)
    if (productEntity.getDetails() != null) {
      JpaProductDetailsEntity jpaProductDetailsEntity = jpaProductEntity.getDetails();
      if (jpaProductDetailsEntity == null) {
        jpaProductDetailsEntity = new JpaProductDetailsEntity();
        jpaProductDetailsEntity.setProduct(jpaProductEntity);
        jpaProductEntity.setDetails(jpaProductDetailsEntity);
      }
      jpaProductDetailsEntity.setDescription(productEntity.getDetails().getDescription());
    }

    // Set attributes (with back-reference)
    if (CollectionUtil.isNotEmpty(productEntity.getAttributes())) {
      for (ProductAttributeEntity productAttributeEntity : productEntity.getAttributes()) {
        JpaProductAttributeEntity jpaProductAttributeEntity = new JpaProductAttributeEntity();
        jpaProductAttributeEntity.setId(productAttributeEntity.getId());
        jpaProductAttributeEntity.setName(productAttributeEntity.getName());
        jpaProductAttributeEntity.setValue(productAttributeEntity.getValue());
        jpaProductEntity.addAttribute(jpaProductAttributeEntity);
      }
    }

    // Set category references by ID only
    if (CollectionUtil.isNotEmpty(productEntity.getCategories())) {
      for (CategoryEntity categoryEntity : productEntity.getCategories()) {
        if (categoryEntity.getId() != null) {
          JpaCategoryEntity jpaCategoryEntity = new JpaCategoryEntity();
          jpaCategoryEntity.setId(categoryEntity.getId());
          jpaProductEntity.addCategory(jpaCategoryEntity);
        }
      }
    }

    return jpaProductEntity;
  }

  public static void merge(ProductEntity productEntity, JpaProductEntity jpaProductEntity) {
    // Update basic fields
    jpaProductEntity.setId(productEntity.getId());
    jpaProductEntity.setName(productEntity.getName());
    jpaProductEntity.setPrice(productEntity.getPrice());
    jpaProductEntity.setQuantity(productEntity.getQuantity());
    jpaProductEntity.setStatus(productEntity.getStatus());
    jpaProductEntity.setAvailableFrom(productEntity.getAvailableFrom());
    jpaProductEntity.setIsActive(productEntity.getIsActive());

    // Update brand reference
    if (productEntity.getBrand() != null && productEntity.getBrand().getId() != null) {
      JpaBrandEntity jpaBrandEntity = new JpaBrandEntity();
      jpaBrandEntity.setId(productEntity.getBrand().getId());
      jpaProductEntity.setBrand(jpaBrandEntity);
    } else {
      jpaProductEntity.setBrand(null);
    }

    // Update details
    if (productEntity.getDetails() != null) {
      JpaProductDetailsEntity jpaDetailsEntity = jpaProductEntity.getDetails();
      if (jpaDetailsEntity == null) {
        jpaDetailsEntity = new JpaProductDetailsEntity();
        jpaDetailsEntity.setProduct(jpaProductEntity);
        jpaProductEntity.setDetails(jpaDetailsEntity);
      }
      jpaDetailsEntity.setDescription(productEntity.getDetails().getDescription());
    } else {
      jpaProductEntity.setDetails(null);
    }

    // Update attributes (update by name, add new, delete unmatched)
    List<JpaProductAttributeEntity> existingJpaAttributeEntities = jpaProductEntity.getAttributes() != null
        ? new ArrayList<>(jpaProductEntity.getAttributes())
        : new ArrayList<>();
    // Clear the attributes list to rebuild it
    jpaProductEntity.getAttributes().clear();
    // Process attributes from productEntity
    if (CollectionUtil.isNotEmpty(productEntity.getAttributes())) {
      for (ProductAttributeEntity attributeEntity : productEntity.getAttributes()) {
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
    if (CollectionUtil.isNotEmpty(productEntity.getCategories())) {
      for (CategoryEntity categoryEntity : productEntity.getCategories()) {
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
    ProductEntity productEntity = new ProductEntity();
    productEntity.setId(jpaProductEntity.getId());
    productEntity.setName(jpaProductEntity.getName());
    productEntity.setPrice(jpaProductEntity.getPrice());
    productEntity.setQuantity(jpaProductEntity.getQuantity());
    productEntity.setStatus(jpaProductEntity.getStatus());
    productEntity.setAvailableFrom(jpaProductEntity.getAvailableFrom());
    productEntity.setIsActive(jpaProductEntity.getIsActive());

    // Brand
    if (jpaProductEntity.getBrand() != null) {
      BrandEntity brandEntity = new BrandEntity();
      brandEntity.setId(jpaProductEntity.getBrand().getId());
      brandEntity.setName(jpaProductEntity.getBrand().getName());
      productEntity.setBrand(brandEntity);
    }

    // Details
    if (jpaProductEntity.getDetails() != null) {
      ProductDetailsEntity productDetailsEntity = new ProductDetailsEntity();
      productDetailsEntity.setDescription(jpaProductEntity.getDetails().getDescription());
      productEntity.setDetails(productDetailsEntity);
    }

    // Attributes
    if (CollectionUtil.isNotEmpty(jpaProductEntity.getAttributes())) {
      for (JpaProductAttributeEntity jpaProductAttributeEntity : jpaProductEntity.getAttributes()) {
        ProductAttributeEntity productAttributeEntity = new ProductAttributeEntity();
        productAttributeEntity.setId(jpaProductAttributeEntity.getId());
        productAttributeEntity.setName(jpaProductAttributeEntity.getName());
        productAttributeEntity.setValue(jpaProductAttributeEntity.getValue());
        productEntity.addAttribute(productAttributeEntity);
      }
    }

    // Categories
    if (CollectionUtil.isNotEmpty(jpaProductEntity.getCategories())) {
      for (JpaCategoryEntity jpaCategoryEntity : jpaProductEntity.getCategories()) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(jpaCategoryEntity.getId());
        categoryEntity.setName(jpaCategoryEntity.getName());
        productEntity.addCategory(categoryEntity);
      }
    }

    return productEntity;
  }
}
