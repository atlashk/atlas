package org.atlas.infrastructure.api.server.rest.adapter.product.admin.mapper;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductAttributeEntity;
import org.atlas.domain.product.entity.ProductDetailsEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminCreateProductRequest;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminUpdateProductRequest;

@UtilityClass
public class AdminProductMapper {

  public static ProductEntity toProductEntity(AdminCreateProductRequest request) {
    // Product
    ProductEntity productEntity = ObjectMapperUtil.getInstance()
        .map(request, ProductEntity.class);

    // Brand
    BrandEntity brandEntity = BrandEntity.builder()
        .id(request.getBrandId())
        .build();
    productEntity.setBrand(brandEntity);

    // Details
    ProductDetailsEntity detailsEntity = ObjectMapperUtil.getInstance()
        .map(request.getDetails(), ProductDetailsEntity.class);
    productEntity.setDetails(detailsEntity);

    // Attributes
    List<ProductAttributeEntity> attributeEntities = ObjectMapperUtil.getInstance()
          .mapList(request.getAttributes(), ProductAttributeEntity.class);
    productEntity.setAttributes(attributeEntities);

    // Categories
    List<CategoryEntity> categoryEntities = request.getCategoryIds()
        .stream()
        .map(id -> CategoryEntity.builder()
            .id(id)
            .build())
        .toList();
    productEntity.setCategories(categoryEntities);

    return productEntity;
  }

  public static ProductEntity toProductEntity(AdminUpdateProductRequest request) {
    // Product
    ProductEntity productEntity = ObjectMapperUtil.getInstance()
        .map(request, ProductEntity.class);

    // Brand
    BrandEntity brandEntity = BrandEntity.builder()
        .id(request.getBrandId())
        .build();
    productEntity.setBrand(brandEntity);

    // Details
    ProductDetailsEntity detailsEntity = ObjectMapperUtil.getInstance()
        .map(request.getDetails(), ProductDetailsEntity.class);
    productEntity.setDetails(detailsEntity);

    // Attributes
    List<ProductAttributeEntity> attributeEntities = ObjectMapperUtil.getInstance()
        .mapList(request.getAttributes(), ProductAttributeEntity.class);
    productEntity.setAttributes(attributeEntities);

    // Categories
    List<CategoryEntity> categoryEntities = request.getCategoryIds()
        .stream()
        .map(id -> CategoryEntity.builder()
            .id(id)
            .build())
        .toList();
    productEntity.setCategories(categoryEntities);

    return productEntity;
  }
}
