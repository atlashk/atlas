package org.atlas.services.catalog.api.rest.category.mapper;

import org.atlas.services.catalog.api.rest.category.model.CategoryResponse;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

  CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

  // Entity/Output --> Response
  // -----------------------------------------------------------------------------------------------

  CategoryResponse toCategoryResponse(CategoryEntity category);
}
