package org.atlas.infrastructure.persistence.jpa.adapter.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.port.repository.BrandRepository;
import org.atlas.domain.product.entity.Brand;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaBrand;
import org.atlas.infrastructure.persistence.jpa.adapter.product.mapper.JpaBrandMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.product.repository.JpaBrandRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaBrandRepositoryAdapter implements BrandRepository {

  private final JpaBrandRepository jpaBrandRepository;

  @Override
  public List<Brand> findAll() {
    List<JpaBrand> jpaBrands = jpaBrandRepository.findAll();
    return ObjectMapperUtil.mapList(jpaBrands, JpaBrandMapper.INSTANCE::toBrand);
  }
}
