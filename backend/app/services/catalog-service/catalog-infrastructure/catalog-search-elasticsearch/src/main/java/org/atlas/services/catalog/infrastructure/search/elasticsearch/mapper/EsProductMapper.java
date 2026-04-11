package org.atlas.services.catalog.infrastructure.search.elasticsearch.mapper;

import org.atlas.services.catalog.domain.entity.Brand;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.domain.entity.ProductAttribute;
import org.atlas.services.catalog.domain.entity.ProductDetails;
import org.atlas.services.catalog.domain.entity.Product;
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
  EsProduct toProductDocument(Product product);

  EsProduct.ProductDetails toProductDetailsDocument(ProductDetails details);

  EsProduct.ProductAttribute toProductAttributeDocument(ProductAttribute attribute);

  EsProduct.Brand toBrandDocument(Brand brand);

  EsProduct.Category toCategoryDocument(Category category);

  // Document --> Entity
  // -----------------------------------------------------------------------------------------------

  @Mapping(target = "id", source = "productId")
  Product toProduct(EsProduct document);

  ProductDetails toProductDetails(EsProduct.ProductDetails details);

  ProductAttribute toProductAttribute(EsProduct.ProductAttribute attribute);

  Brand toBrand(EsProduct.Brand brand);

  Category toCategory(EsProduct.Category category);
}
