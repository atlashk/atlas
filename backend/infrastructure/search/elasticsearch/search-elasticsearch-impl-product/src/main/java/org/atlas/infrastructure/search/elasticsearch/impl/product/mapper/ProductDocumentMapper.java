package org.atlas.infrastructure.search.elasticsearch.impl.product.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductAttributeEntity;
import org.atlas.domain.product.entity.ProductDetailsEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.infrastructure.search.elasticsearch.impl.product.document.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductDocumentMapper {

  public ProductDocument toProductDocument(ProductEntity product) {
    if (product == null) {
      return null;
    }

    // Map product details
    ProductDocument.ProductDetails details = null;
    if (product.getDetails() != null) {
      details = ProductDocument.ProductDetails.builder()
          .description(product.getDetails().getDescription())
          .build();
    }

    // Map product attributes
    List<ProductDocument.ProductAttribute> attributes = null;
    if (product.getAttributes() != null) {
      attributes = product.getAttributes().stream()
          .map(attribute -> ProductDocument.ProductAttribute.builder()
              .id(attribute.getId())
              .name(attribute.getName())
              .value(attribute.getValue())
              .build())
          .collect(Collectors.toList());
    }

    // Map brand
    ProductDocument.Brand brand = null;
    if (product.getBrand() != null) {
      brand = ProductDocument.Brand.builder()
          .id(product.getBrand().getId())
          .name(product.getBrand().getName())
          .build();
    }

    // Map categories
    List<ProductDocument.Category> categories = null;
    if (product.getCategories() != null) {
      categories = product.getCategories().stream()
          .map(category -> ProductDocument.Category.builder()
              .id(category.getId())
              .name(category.getName())
              .build())
          .collect(Collectors.toList());
    }

    return ProductDocument.builder()
        .id(product.getId() != null ? product.getId().toString() : null)
        .productId(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .status(product.getStatus())
        .details(details)
        .attributes(attributes)
        .brand(brand)
        .categories(categories)
        .build();
  }

  public ProductEntity toProductEntity(ProductDocument document) {
    if (document == null) {
      return null;
    }

    // Map product details
    ProductDetailsEntity details = null;
    if (document.getDetails() != null) {
      details = ProductDetailsEntity.builder()
          .description(document.getDetails().getDescription())
          .build();
    }

    // Map attributes
    List<ProductAttributeEntity> attributes = null;
    if (document.getAttributes() != null) {
      attributes = document.getAttributes().stream()
          .map(attribute -> ProductAttributeEntity.builder()
              .id(attribute.getId())
              .name(attribute.getName())
              .value(attribute.getValue())
              .build())
          .collect(Collectors.toList());
    }

    // Map brand
    BrandEntity brand = null;
    if (document.getBrand() != null) {
      brand = BrandEntity.builder()
          .id(document.getBrand().getId())
          .name(document.getBrand().getName())
          .build();
    }

    // Map categories
    List<CategoryEntity> categories = null;
    if (document.getCategories() != null) {
      categories = document.getCategories().stream()
          .map(category -> CategoryEntity.builder()
              .id(category.getId())
              .name(category.getName())
              .build())
          .collect(Collectors.toList());
    }

    return ProductEntity.builder()
        .id(document.getProductId())
        .name(document.getName())
        .price(document.getPrice())
        .status(document.getStatus())
        .details(details)
        .attributes(attributes)
        .brand(brand)
        .categories(categories)
        .build();
  }
}