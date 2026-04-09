package org.atlas.services.catalog.infrastructure.search.elasticsearch.mapper;

import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.domain.entity.ProductAttributeEntity;
import org.atlas.services.catalog.domain.entity.ProductDetailsEntity;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.document.EsProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EsProductMapper {

  EsProductMapper INSTANCE = Mappers.getMapper(EsProductMapper.class);

  // Entity --> Document
  // -----------------------------------------------------------------------------------------------

  @Mapping(target = "id", source = "id")
  @Mapping(target = "productId", source = "id")
  EsProduct toProductDocument(ProductEntity product);

  EsProduct.ProductDetails toProductDetailsDocument(ProductDetailsEntity details);

  EsProduct.ProductAttribute toProductAttributeDocument(ProductAttributeEntity attribute);

  EsProduct.Brand toBrandDocument(BrandEntity brand);

  EsProduct.Category toCategoryDocument(CategoryEntity category);

  // Document --> Entity
  // -----------------------------------------------------------------------------------------------

  @Mapping(target = "id", source = "productId")
  ProductEntity toProduct(EsProduct document);

  ProductDetailsEntity toProductDetails(EsProduct.ProductDetails details);

  ProductAttributeEntity toProductAttribute(EsProduct.ProductAttribute attribute);

  BrandEntity toBrand(EsProduct.Brand brand);

  CategoryEntity toCategory(EsProduct.Category category);
}
