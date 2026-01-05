package org.atlas.product.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.product.application.port.repository.CategoryRepository;
import org.atlas.product.domain.entity.Category;
import org.atlas.common.framework.util.ObjectMapperUtil;
import org.atlas.product.persistence.jpa.entity.JpaCategory;
import org.atlas.product.persistence.jpa.mapper.JpaCategoryMapper;
import org.atlas.product.persistence.jpa.repository.JpaCategoryRepository;
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
