package org.atlas.services.catalog.api.rest.product.mapper;

import java.util.List;
import org.atlas.services.catalog.api.rest.product.model.admin.CreateProductRequest;
import org.atlas.services.catalog.api.rest.product.model.admin.ExportProductRequest;
import org.atlas.services.catalog.api.rest.product.model.admin.ProductResponse;
import org.atlas.services.catalog.api.rest.product.model.admin.RetrieveProductListRequest;
import org.atlas.services.catalog.api.rest.product.model.admin.UpdateProductRequest;
import org.atlas.services.catalog.domain.entity.Brand;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.domain.entity.ProductAttribute;
import org.atlas.services.catalog.domain.entity.ProductDetails;
import org.atlas.services.catalog.domain.entity.Product;
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
  Product toProduct(CreateProductRequest request);

  ProductDetails toProductDetails(CreateProductRequest.ProductDetails details);

  ProductAttribute toProductAttribute(CreateProductRequest.ProductAttribute attribute);

  @Mapping(target = "brand", source = "brandId")
  @Mapping(target = "categories", source = "categoryIds")
  @Mapping(target = "id", ignore = true)
  Product toProduct(UpdateProductRequest request);

  ProductDetails toProductDetails(UpdateProductRequest.ProductDetails details);

  ProductAttribute toProductAttribute(UpdateProductRequest.ProductAttribute attribute);

  ExportProductInput toExportProductInput(ExportProductRequest request);

  // Helper methods for mapping complex objects
  default Brand mapBrandId(String brandId) {
    if (brandId == null) {
      return null;
    }
    return Brand.builder()
        .id(brandId)
        .build();
  }

  default List<Category> mapCategoryIds(List<String> categoryIds) {
    if (categoryIds == null) {
      return null;
    }
    return categoryIds.stream()
        .map(id -> Category.builder()
            .id(id)
            .build())
        .toList();
  }

  // Entity/Output --> Response
  // -----------------------------------------------------------------------------------------------

  ProductResponse toProductResponse(Product product);
}
