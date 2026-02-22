package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.product.domain.entity.BrandEntity;
import org.atlas.services.catalog.port.out.repository.BrandRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaBrandEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.JpaBrandMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.JpaBrandRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaBrandRepositoryAdapter implements BrandRepository {

  private final JpaBrandRepository jpaBrandRepository;

  @Override
  public List<BrandEntity> findAll() {
    List<JpaBrandEntity> jpaBrands = jpaBrandRepository.findAll();
    return MapperUtil.mapList(jpaBrands, JpaBrandMapper.INSTANCE::toBrand);
  }
}
