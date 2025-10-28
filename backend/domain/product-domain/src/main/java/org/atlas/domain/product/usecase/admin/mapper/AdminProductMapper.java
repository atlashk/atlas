package org.atlas.domain.product.usecase.admin.mapper;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.entity.Category;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.usecase.admin.model.AdminExportProductInput;
import org.atlas.domain.product.usecase.admin.model.AdminListProductInput;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  FindProductCriteria toFindProductCriteria(AdminListProductInput input);

  FindProductCriteria toFindProductCriteria(AdminExportProductInput input);

  Product toProduct(ProductRow row);

  @AfterMapping
  default void afterToProduct(ProductRow row, @MappingTarget Product product) {
    // Brand
    Brand brand = new Brand();
    brand.setId(row.getBrandId());
    product.setBrand(brand);

    // Categories
    List<Category> categories = Arrays.stream(row.getCategoryIds().split("\\|"))
        .filter(StringUtils::isNotBlank)
        .map(categoryIdStr -> {
          Category category = new Category();
          category.setId(Integer.parseInt(categoryIdStr.trim()));
          return category;
        })
        .toList();
    product.setCategories(categories);
  }

  org.atlas.framework.domain.event.contract.product.model.Product toProduct(Product product);

  org.atlas.domain.product.infrastructure.file.model.write.ProductRow toProductRow(Product product);

  void merge(Product source, @MappingTarget Product target);
}
