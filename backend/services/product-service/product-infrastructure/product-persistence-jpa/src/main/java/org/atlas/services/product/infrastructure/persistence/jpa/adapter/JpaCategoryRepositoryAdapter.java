package org.atlas.services.product.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.port.out.repository.CategoryRepository;
import org.atlas.services.product.domain.entity.Category;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaCategory;
import org.atlas.services.product.infrastructure.persistence.jpa.mapper.JpaCategoryMapper;
import org.atlas.services.product.infrastructure.persistence.jpa.repository.JpaCategoryRepository;
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
