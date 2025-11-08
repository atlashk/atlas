package org.atlas.infrastructure.search.elasticsearch.impl.product.mapper;

import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.entity.Category;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.entity.ProductAttribute;
import org.atlas.domain.product.entity.ProductDetails;
import org.atlas.infrastructure.search.elasticsearch.impl.product.document.ElasticsearchProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface ElasticsearchProductMapper {

  @Mapping(target = "productId", source = "id")
  ElasticsearchProduct toProductDocument(Product product);

  ElasticsearchProduct.ProductDetails toProductDetailsDocument(ProductDetails details);

  ElasticsearchProduct.ProductAttribute toProductAttributeDocument(ProductAttribute attribute);

  ElasticsearchProduct.Brand toBrandDocument(Brand brand);

  ElasticsearchProduct.Category toCategoryDocument(Category category);

  @Mapping(target = "id", source = "productId")
  Product toProduct(ElasticsearchProduct document);

  ProductDetails toProductDetails(ElasticsearchProduct.ProductDetails details);

  ProductAttribute toProductAttribute(ElasticsearchProduct.ProductAttribute attribute);

  Brand toBrand(ElasticsearchProduct.Brand brand);

  Category toCategory(ElasticsearchProduct.Category category);
}