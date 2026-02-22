package org.atlas.services.product.infrastructure.api.server.rest.front.mapper;

import org.atlas.services.product.infrastructure.api.server.rest.front.model.CategoryResponse;
import org.atlas.services.product.domain.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

  CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

  CategoryResponse toCategoryResponse(CategoryEntity category);
}
