package org.atlas.services.product.api.server.rest.mapper;

import org.atlas.services.product.api.server.rest.model.CategoryResponse;
import org.atlas.services.product.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

  CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

  CategoryResponse toCategoryResponse(Category category);
}
