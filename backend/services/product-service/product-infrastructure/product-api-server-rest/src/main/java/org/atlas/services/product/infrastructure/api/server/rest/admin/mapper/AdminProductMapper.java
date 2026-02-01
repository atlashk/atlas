package org.atlas.services.product.infrastructure.api.server.rest.admin.mapper;

import java.util.List;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminCreateProductRequest;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminProductResponse;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminUpdateProductRequest;
import org.atlas.services.product.domain.entity.Brand;
import org.atlas.services.product.domain.entity.Category;
import org.atlas.services.product.domain.entity.Product;
import org.atlas.services.product.domain.entity.ProductAttribute;
import org.atlas.services.product.domain.entity.ProductDetails;
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
