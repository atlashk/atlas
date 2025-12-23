package org.atlas.infrastructure.api.server.rest.adapter.product.admin.mapper;

import java.util.List;
import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.entity.Category;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.entity.ProductAttribute;
import org.atlas.domain.product.entity.ProductDetails;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminCreateProductRequest;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminProductResponse;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminUpdateProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  AdminProductResponse toProductResponse(Product product);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "id", ignore = true)
  Product toProduct(AdminCreateProductRequest request);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "id", ignore = true)
  Product toProduct(AdminUpdateProductRequest request);

  // Helper methods for mapping complex objects
  default Brand mapBrandId(Integer brandId) {
    if (brandId == null) {
      return null;
    }
    return Brand.builder()
        .id(brandId)
        .build();
  }

  default List<Category> mapCategoryIds(List<Integer> categoryIds) {
    if (categoryIds == null) {
      return null;
    }
    return categoryIds.stream()
        .map(id -> Category.builder()
            .id(id)
            .build())
        .toList();
  }

  // Mapping for nested objects
  ProductDetails toProductDetails(AdminCreateProductRequest.ProductDetails details);

  ProductAttribute toProductAttribute(AdminCreateProductRequest.ProductAttribute attribute);

  ProductDetails toProductDetails(AdminUpdateProductRequest.ProductDetails details);

  ProductAttribute toProductAttribute(AdminUpdateProductRequest.ProductAttribute attribute);
}
