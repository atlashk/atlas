package org.atlas.product.fulltextsearch.elasticsearch.mapper;

import org.atlas.product.domain.entity.Brand;
import org.atlas.product.domain.entity.Category;
import org.atlas.product.domain.entity.Product;
import org.atlas.product.domain.entity.ProductAttribute;
import org.atlas.product.domain.entity.ProductDetails;
import org.atlas.product.fulltextsearch.elasticsearch.document.ElasticsearchProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElasticsearchProductMapper {

  ElasticsearchProductMapper INSTANCE = Mappers.getMapper(ElasticsearchProductMapper.class);

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
