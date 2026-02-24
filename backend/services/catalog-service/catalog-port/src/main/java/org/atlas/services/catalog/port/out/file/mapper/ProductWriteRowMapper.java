package org.atlas.services.catalog.port.out.file.mapper;

import java.util.stream.Collectors;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductWriteRowMapper {

  ProductWriteRowMapper INSTANCE = Mappers.getMapper(ProductWriteRowMapper.class);

  ProductWriteRow toProductWriteRow(ProductEntity product);

  @AfterMapping
  default void afterToProductWriteRow(ProductEntity product, @MappingTarget ProductWriteRow row) {
    // Brand name
    if (product.getBrand() != null) {
      row.setBrandName(product.getBrand().getName());  
    }

    // Category names
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      String categoryIds = product.getCategories().stream()
          .map(CategoryEntity::getName)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryNames(categoryIds);
    }
  }
}
