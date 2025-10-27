package org.atlas.infrastructure.persistence.jpa.impl.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.repository.CategoryRepository;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.JpaCategoryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCategoryRepositoryAdapter implements CategoryRepository {

  private final JpaCategoryRepository jpaCategoryRepository;

  @Override
  public List<CategoryEntity> findAll() {
    return jpaCategoryRepository.findAll()
        .stream()
        .map(jpaCategory -> ObjectMapperUtil.getInstance()
            .map(jpaCategory, CategoryEntity.class))
        .toList();
  }
}
