package org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.mapper;

import org.atlas.services.product.domain.entity.BrandEntity;
import org.atlas.services.product.domain.entity.CategoryEntity;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.domain.entity.ProductAttributeEntity;
import org.atlas.services.product.domain.entity.ProductDetailsEntity;
import org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.document.ElasticsearchProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElasticsearchProductMapper {

  ElasticsearchProductMapper INSTANCE = Mappers.getMapper(ElasticsearchProductMapper.class);

  @Mapping(target = "productId", source = "id")
  ElasticsearchProduct toProductDocument(ProductEntity product);

  ElasticsearchProduct.ProductDetails toProductDetailsDocument(ProductDetailsEntity details);

  ElasticsearchProduct.ProductAttribute toProductAttributeDocument(ProductAttributeEntity attribute);

  ElasticsearchProduct.Brand toBrandDocument(BrandEntity brand);

  ElasticsearchProduct.Category toCategoryDocument(CategoryEntity category);

  @Mapping(target = "id", source = "productId")
  ProductEntity toProduct(ElasticsearchProduct document);

  ProductDetailsEntity toProductDetails(ElasticsearchProduct.ProductDetails details);

  ProductAttributeEntity toProductAttribute(ElasticsearchProduct.ProductAttribute attribute);

  BrandEntity toBrand(ElasticsearchProduct.Brand brand);

  CategoryEntity toCategory(ElasticsearchProduct.Category category);
}
