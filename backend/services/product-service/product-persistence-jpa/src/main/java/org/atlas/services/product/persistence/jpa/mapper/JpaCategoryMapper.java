package org.atlas.services.product.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.Category;
import org.atlas.services.product.persistence.jpa.entity.JpaCategory;
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

  JpaCategory toJpaCategory(Category category);

  Category toCategory(JpaCategory jpaCategory);
}
