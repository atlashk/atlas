package org.atlas.infrastructure.api.server.rest.impl.product.front.mapper;

import org.atlas.domain.product.entity.Category;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

  CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

  CategoryResponse toCategoryResponse(Category category);
}
