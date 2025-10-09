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
    JpaProductEntity jpaProduct = new JpaProductEntity();
    jpaProduct.setId(product.getId());
    jpaProduct.setName(product.getName());
    jpaProduct.setPrice(product.getPrice());
    jpaProduct.setQuantity(product.getQuantity());
    jpaProduct.setStatus(product.getStatus());
    jpaProduct.setAvailableFrom(product.getAvailableFrom());
    jpaProduct.setIsActive(product.getIsActive());

    // Set brand reference by ID only
    if (product.getBrand() != null && product.getBrand().getId() != null) {
      JpaBrandEntity jpaBrandEntity = new JpaBrandEntity();
      jpaBrandEntity.setId(product.getBrand().getId());
      jpaProduct.setBrand(jpaBrandEntity);
    }

    // Set details (with back-reference)
    if (product.getDetails() != null) {
      JpaProductDetailsEntity jpaProductDetailsEntity = jpaProduct.getDetails();
      if (jpaProductDetailsEntity == null) {
        jpaProductDetailsEntity = new JpaProductDetailsEntity();
        jpaProductDetailsEntity.setProduct(jpaProduct);
        jpaProduct.setDetails(jpaProductDetailsEntity);
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
        jpaProduct.addAttribute(jpaProductAttributeEntity);
      }
    }

    // Set category references by ID only
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      for (CategoryEntity categoryEntity : product.getCategories()) {
        if (categoryEntity.getId() != null) {
          JpaCategoryEntity jpaCategoryEntity = new JpaCategoryEntity();
          jpaCategoryEntity.setId(categoryEntity.getId());
          jpaProduct.addCategory(jpaCategoryEntity);
        }
      }
    }

    return jpaProduct;
  }

  public static void merge(ProductEntity product, JpaProductEntity jpaProduct) {
    // Update basic fields
    jpaProduct.setId(product.getId());
    jpaProduct.setName(product.getName());
    jpaProduct.setPrice(product.getPrice());
    jpaProduct.setQuantity(product.getQuantity());
    jpaProduct.setStatus(product.getStatus());
    jpaProduct.setAvailableFrom(product.getAvailableFrom());
    jpaProduct.setIsActive(product.getIsActive());

    // Update brand reference
    if (product.getBrand() != null && product.getBrand().getId() != null) {
      JpaBrandEntity jpaBrandEntity = new JpaBrandEntity();
      jpaBrandEntity.setId(product.getBrand().getId());
      jpaProduct.setBrand(jpaBrandEntity);
    } else {
      jpaProduct.setBrand(null);
    }

    // Update details
    if (product.getDetails() != null) {
      JpaProductDetailsEntity jpaDetailsEntity = jpaProduct.getDetails();
      if (jpaDetailsEntity == null) {
        jpaDetailsEntity = new JpaProductDetailsEntity();
        jpaDetailsEntity.setProduct(jpaProduct);
        jpaProduct.setDetails(jpaDetailsEntity);
      }
      jpaDetailsEntity.setDescription(product.getDetails().getDescription());
    } else {
      jpaProduct.setDetails(null);
    }

    // Update attributes (update by name, add new, delete unmatched)
    List<JpaProductAttributeEntity> existingJpaAttributeEntities =
        jpaProduct.getAttributes() != null
            ? new ArrayList<>(jpaProduct.getAttributes())
            : new ArrayList<>();
    // Clear the attributes list to rebuild it
    jpaProduct.getAttributes().clear();
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
          jpaProduct.addAttribute(jpaAttributeEntity);
        } else {
          // Add new attribute
          JpaProductAttributeEntity jpaAttributeEntity = new JpaProductAttributeEntity();
          jpaAttributeEntity.setName(attributeEntity.getName());
          jpaAttributeEntity.setValue(attributeEntity.getValue());
          jpaProduct.addAttribute(jpaAttributeEntity);
        }
      }
    }

    // Update categories
    jpaProduct.getCategories().clear();
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      for (CategoryEntity categoryEntity : product.getCategories()) {
        if (categoryEntity.getId() != null) {
          JpaCategoryEntity jpaCategoryEntity = new JpaCategoryEntity();
          jpaCategoryEntity.setId(categoryEntity.getId());
          jpaProduct.addCategory(jpaCategoryEntity);
        }
      }
    }
  }

  public static ProductEntity toProductEntity(JpaProductEntity jpaProduct) {
    // Product
    ProductEntity product = new ProductEntity();
    product.setId(jpaProduct.getId());
    product.setName(jpaProduct.getName());
    product.setPrice(jpaProduct.getPrice());
    product.setQuantity(jpaProduct.getQuantity());
    product.setStatus(jpaProduct.getStatus());
    product.setAvailableFrom(jpaProduct.getAvailableFrom());
    product.setIsActive(jpaProduct.getIsActive());

    // Brand
    if (jpaProduct.getBrand() != null) {
      BrandEntity brandEntity = new BrandEntity();
      brandEntity.setId(jpaProduct.getBrand().getId());
      brandEntity.setName(jpaProduct.getBrand().getName());
      product.setBrand(brandEntity);
    }

    // Details
    if (jpaProduct.getDetails() != null) {
      ProductDetailsEntity productDetailsEntity = new ProductDetailsEntity();
      productDetailsEntity.setDescription(jpaProduct.getDetails().getDescription());
      product.setDetails(productDetailsEntity);
    }

    // Attributes
    if (CollectionUtil.isNotEmpty(jpaProduct.getAttributes())) {
      for (JpaProductAttributeEntity jpaProductAttributeEntity : jpaProduct.getAttributes()) {
        ProductAttributeEntity productAttributeEntity = new ProductAttributeEntity();
        productAttributeEntity.setId(jpaProductAttributeEntity.getId());
        productAttributeEntity.setName(jpaProductAttributeEntity.getName());
        productAttributeEntity.setValue(jpaProductAttributeEntity.getValue());
        product.addAttribute(productAttributeEntity);
      }
    }

    // Categories
    if (CollectionUtil.isNotEmpty(jpaProduct.getCategories())) {
      for (JpaCategoryEntity jpaCategoryEntity : jpaProduct.getCategories()) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(jpaCategoryEntity.getId());
        categoryEntity.setName(jpaCategoryEntity.getName());
        product.addCategory(categoryEntity);
      }
    }

    return product;
  }
}
