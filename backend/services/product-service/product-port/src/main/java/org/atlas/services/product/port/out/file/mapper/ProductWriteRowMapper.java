package org.atlas.services.product.port.out.file.mapper;

import java.util.stream.Collectors;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.out.file.model.ProductWriteRow;
import org.atlas.services.product.domain.entity.CategoryEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductWriteRowMapper {

  ProductWriteRowMapper INSTANCE = Mappers.getMapper(ProductWriteRowMapper.class);

  @Mapping(target = "brandId", source = "brand.id")
  ProductWriteRow toProductWriteRow(ProductEntity product);

  @AfterMapping
  default void afterToProductWriteRow(ProductEntity product, @MappingTarget ProductWriteRow row) {
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      String categoryIds = product.getCategories().stream()
          .map(CategoryEntity::getId)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryIds(categoryIds);
    }
  }
}
