package org.atlas.services.catalog.infrastructure.api.server.rest.product.mapper;

import java.util.List;
import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.domain.entity.ProductAttributeEntity;
import org.atlas.services.catalog.domain.entity.ProductDetailsEntity;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.CreateProductRequest;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.ExportProductRequest;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.ProductResponse;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.RetrieveProductListRequest;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.UpdateProductRequest;
import org.atlas.services.catalog.port.in.product.model.admin.ExportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.RetrieveProductListInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductAdminMapper {

  ProductAdminMapper INSTANCE = Mappers.getMapper(ProductAdminMapper.class);

  // Request --> Input/Entity
  // -----------------------------------------------------------------------------------------------

  RetrieveProductListInput toRetrieveProductListInput(RetrieveProductListRequest request);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "id", ignore = true)
  ProductEntity toProduct(CreateProductRequest request);

  ProductDetailsEntity toProductDetails(CreateProductRequest.ProductDetails details);

  ProductAttributeEntity toProductAttribute(CreateProductRequest.ProductAttribute attribute);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "id", ignore = true)
  ProductEntity toProduct(UpdateProductRequest request);

  ProductDetailsEntity toProductDetails(UpdateProductRequest.ProductDetails details);

  ProductAttributeEntity toProductAttribute(UpdateProductRequest.ProductAttribute attribute);

  ExportProductInput toExportProductInput(ExportProductRequest request);

  // Helper methods for mapping complex objects
  default BrandEntity mapBrandId(String brandId) {
    if (brandId == null) {
      return null;
    }
    return BrandEntity.builder()
        .id(brandId)
        .build();
  }

  default List<CategoryEntity> mapCategoryIds(List<String> categoryIds) {
    if (categoryIds == null) {
      return null;
    }
    return categoryIds.stream()
        .map(id -> CategoryEntity.builder()
            .id(id)
            .build())
        .toList();
  }

  // Entity/Output --> Response
  // -----------------------------------------------------------------------------------------------

  ProductResponse toProductResponse(ProductEntity product);
}
