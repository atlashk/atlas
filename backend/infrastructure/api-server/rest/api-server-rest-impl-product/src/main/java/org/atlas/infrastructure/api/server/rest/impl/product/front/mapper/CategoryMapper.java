package org.atlas.infrastructure.api.server.rest.impl.product.front.mapper;

import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.entity.Category;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.BrandResponse;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryMapper {

  CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

  CategoryResponse toCategoryResponse(Category category);
}
