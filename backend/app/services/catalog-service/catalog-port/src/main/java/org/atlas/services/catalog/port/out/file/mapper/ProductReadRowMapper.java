package org.atlas.services.catalog.port.out.file.mapper;

import java.util.List;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.catalog.domain.entity.Brand;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.out.file.model.ProductReadRow;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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
        .filter(StringUtil::isNotBlank)
        .map(categoryIdStr -> {
          Category category = new Category();
          category.setId(categoryIdStr);
          return category;
        })
        .toList();
    product.setCategories(categories);
  }
}
