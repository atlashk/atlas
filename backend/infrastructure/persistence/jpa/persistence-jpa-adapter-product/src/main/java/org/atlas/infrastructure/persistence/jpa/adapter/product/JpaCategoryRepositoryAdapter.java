package org.atlas.infrastructure.persistence.jpa.adapter.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.port.repository.CategoryRepository;
import org.atlas.domain.product.entity.Category;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaCategory;
import org.atlas.infrastructure.persistence.jpa.adapter.product.mapper.JpaCategoryMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.product.repository.JpaCategoryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCategoryRepositoryAdapter implements CategoryRepository {

  private final JpaCategoryRepository jpaCategoryRepository;

  @Override
  public List<Category> findAll() {
    List<JpaCategory> jpaCategories = jpaCategoryRepository.findAll();
    return ObjectMapperUtil.mapList(jpaCategories, JpaCategoryMapper.INSTANCE::toCategory);
  }
}
