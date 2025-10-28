package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import org.atlas.domain.product.entity.Category;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaCategory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaCategoryMapper {

  JpaCategoryMapper INSTANCE = Mappers.getMapper(JpaCategoryMapper.class);

  JpaCategory toJpaCategory(Category category);

  Category toCategory(JpaCategory jpaCategory);
}
