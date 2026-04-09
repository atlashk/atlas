package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaCategoryEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.JpaCategoryMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.JpaCategoryRepository;
import org.atlas.services.catalog.port.out.repository.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCategoryRepositoryAdapter implements CategoryRepository {

  private final JpaCategoryRepository jpaCategoryRepository;

  @Override
  public List<CategoryEntity> findAll() {
    List<JpaCategoryEntity> jpaCategories = jpaCategoryRepository.findAll();
    return MapperUtil.mapList(jpaCategories, JpaCategoryMapper.INSTANCE::toCategory);
  }
}
