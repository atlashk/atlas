package org.atlas.infrastructure.persistence.jpa.adapter.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.repository.BrandRepository;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.product.repository.JpaBrandRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaBrandRepositoryAdapter implements BrandRepository {

  private final JpaBrandRepository jpaBrandRepository;

  @Override
  public List<BrandEntity> findAll() {
    return jpaBrandRepository.findAll()
        .stream()
        .map(jpaBrand -> ObjectMapperUtil.getInstance()
            .map(jpaBrand, BrandEntity.class))
        .toList();
  }
}
