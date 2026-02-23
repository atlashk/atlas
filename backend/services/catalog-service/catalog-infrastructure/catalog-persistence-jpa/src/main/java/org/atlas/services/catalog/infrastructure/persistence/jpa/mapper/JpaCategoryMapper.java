package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaCategoryEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaCategoryMapper {

  JpaCategoryMapper INSTANCE = Mappers.getMapper(JpaCategoryMapper.class);

  JpaCategoryEntity toJpaCategory(CategoryEntity category);

  CategoryEntity toCategory(JpaCategoryEntity jpaCategory);
}
