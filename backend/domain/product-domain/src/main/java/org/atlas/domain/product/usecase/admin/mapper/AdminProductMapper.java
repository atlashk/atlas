package org.atlas.domain.product.usecase.admin.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.entity.Category;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.usecase.admin.model.AdminExportProductInput;
import org.atlas.domain.product.usecase.admin.model.AdminListProductInput;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.util.StringUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  FindProductCriteria toFindProductCriteria(AdminListProductInput input);

  FindProductCriteria toFindProductCriteria(AdminExportProductInput input);

  Product toProduct(ProductRow row);

  org.atlas.framework.domain.event.contract.product.model.Product toProduct(Product product);

  @AfterMapping
  default void afterToProduct(ProductRow row, @MappingTarget Product product) {
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

  @Mapping(target = "brandId", source = "brand.id")
  org.atlas.domain.product.infrastructure.file.model.write.ProductRow toProductRow(Product product);

  @AfterMapping
  default void afterToProductRow(Product product,
      @MappingTarget org.atlas.domain.product.infrastructure.file.model.write.ProductRow productRow) {
    if (CollectionUtil.isNotEmpty(product.getCategories())) {
      String categoryIds = product.getCategories().stream()
          .map(Category::getId)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      productRow.setCategoryIds(categoryIds);
    }
  }

  void merge(Product source, @MappingTarget Product target);
}
