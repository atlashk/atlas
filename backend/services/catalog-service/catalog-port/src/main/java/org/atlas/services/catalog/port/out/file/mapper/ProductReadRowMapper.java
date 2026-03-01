package org.atlas.services.catalog.port.out.file.mapper;

import java.util.List;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.out.file.model.ProductReadRow;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductReadRowMapper {

  ProductReadRowMapper INSTANCE = Mappers.getMapper(ProductReadRowMapper.class);

  ProductEntity toProduct(ProductReadRow row);

  @AfterMapping
  default void afterToProduct(ProductReadRow row, @MappingTarget ProductEntity product) {
    // Brand
    BrandEntity brand = new BrandEntity();
    brand.setId(row.getBrandId());
    product.setBrand(brand);

    // Categories
    List<CategoryEntity> categories = StringUtil.split(row.getCategoryIds(), "\\|")
        .stream()
        .filter(StringUtil::isNotBlank)
        .map(categoryIdStr -> {
          CategoryEntity category = new CategoryEntity();
          category.setId(StringUtil.trim(categoryIdStr));
          return category;
        })
        .toList();
    product.setCategories(categories);
  }
}
