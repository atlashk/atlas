package org.atlas.services.product.port.out.file.mapper;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.product.domain.entity.CategoryEntity;
import org.atlas.services.product.port.out.file.model.ProductReadRow;
import org.atlas.services.product.domain.entity.BrandEntity;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
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
        .filter(StringUtils::isNotBlank)
        .map(categoryIdStr -> {
          CategoryEntity category = new CategoryEntity();
          category.setId(Integer.parseInt(categoryIdStr.trim()));
          return category;
        })
        .toList();
    product.setCategories(categories);
  }
}
