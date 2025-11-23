package org.atlas.domain.product.infrastructure.file.mapper;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.entity.Category;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.file.model.ProductReadRow;
import org.atlas.framework.util.StringUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductReadRowMapper {

  ProductReadRowMapper INSTANCE = Mappers.getMapper(ProductReadRowMapper.class);

  Product toProduct(ProductReadRow row);

  @AfterMapping
  default void afterToProduct(ProductReadRow row, @MappingTarget Product product) {
    // Brand
    Brand brand = new Brand();
    brand.setId(row.getBrandId());
    product.setBrand(brand);

    // Categories
    List<Category> categories = StringUtil.split(row.getCategoryIds(), "\\|")
        .stream()
        .filter(StringUtils::isNotBlank)
        .map(categoryIdStr -> {
          Category category = new Category();
          category.setId(Integer.parseInt(categoryIdStr.trim()));
          return category;
        })
        .toList();
    product.setCategories(categories);
  }
}
