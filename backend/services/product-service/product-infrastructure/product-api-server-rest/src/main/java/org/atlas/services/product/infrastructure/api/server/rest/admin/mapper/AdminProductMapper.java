package org.atlas.services.product.infrastructure.api.server.rest.admin.mapper;

import java.util.List;
import org.atlas.services.product.domain.entity.CategoryEntity;
import org.atlas.services.product.domain.entity.ProductAttributeEntity;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminCreateProductRequest;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminProductResponse;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminUpdateProductRequest;
import org.atlas.services.product.domain.entity.BrandEntity;
import org.atlas.services.product.domain.entity.ProductDetailsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  AdminProductResponse toProductResponse(ProductEntity product);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "productId", ignore = true)
  ProductEntity toProduct(AdminCreateProductRequest request);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "productId", ignore = true)
  ProductEntity toProduct(AdminUpdateProductRequest request);

  // Helper methods for mapping complex objects
  default BrandEntity mapBrandId(Integer brandId) {
    if (brandId == null) {
      return null;
    }
    return BrandEntity.builder()
        .id(brandId)
        .build();
  }

  default List<CategoryEntity> mapCategoryIds(List<Integer> categoryIds) {
    if (categoryIds == null) {
      return null;
    }
    return categoryIds.stream()
        .map(id -> CategoryEntity.builder()
            .id(id)
            .build())
        .toList();
  }

  // Mapping for nested objects
  ProductDetailsEntity toProductDetails(AdminCreateProductRequest.ProductDetails details);

  ProductAttributeEntity toProductAttribute(AdminCreateProductRequest.ProductAttribute attribute);

  ProductDetailsEntity toProductDetails(AdminUpdateProductRequest.ProductDetails details);

  ProductAttributeEntity toProductAttribute(AdminUpdateProductRequest.ProductAttribute attribute);
}
