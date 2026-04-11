package org.atlas.services.catalog.port.out.file.mapper;

import java.util.stream.Collectors;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductWriteRowMapper {

  ProductWriteRowMapper INSTANCE = Mappers.getMapper(ProductWriteRowMapper.class);

  ProductWriteRow toProductWriteRow(Product product);

  @AfterMapping
  default void afterToProductWriteRow(Product product, @MappingTarget ProductWriteRow row) {
    // Brand name
    if (product.getBrand() != null) {
      row.setBrandName(product.getBrand().getName());  
    }

    // Category names
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      String categoryIds = product.getCategories().stream()
          .map(Category::getName)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryNames(categoryIds);
    }
  }
}
