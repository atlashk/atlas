package org.atlas.product.persistence.jpa.mapper;

import org.atlas.product.domain.entity.Category;
import org.atlas.product.persistence.jpa.entity.JpaCategory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaCategoryMapper {

  JpaCategoryMapper INSTANCE = Mappers.getMapper(JpaCategoryMapper.class);

  JpaCategory toJpaCategory(Category category);

  Category toCategory(JpaCategory jpaCategory);
}
