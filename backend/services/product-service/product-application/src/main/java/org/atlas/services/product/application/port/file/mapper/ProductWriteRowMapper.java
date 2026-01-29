package org.atlas.services.product.application.port.file.mapper;

import java.util.stream.Collectors;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.product.application.port.file.model.ProductWriteRow;
import org.atlas.services.product.domain.entity.Category;
import org.atlas.services.product.domain.entity.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductWriteRowMapper {

  ProductWriteRowMapper INSTANCE = Mappers.getMapper(ProductWriteRowMapper.class);

  @Mapping(target = "brandId", source = "brand.id")
  ProductWriteRow toProductWriteRow(Product product);

  @AfterMapping
  default void afterToProductWriteRow(Product product, @MappingTarget ProductWriteRow row) {
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      String categoryIds = product.getCategories().stream()
          .map(Category::getId)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryIds(categoryIds);
    }
  }
}
