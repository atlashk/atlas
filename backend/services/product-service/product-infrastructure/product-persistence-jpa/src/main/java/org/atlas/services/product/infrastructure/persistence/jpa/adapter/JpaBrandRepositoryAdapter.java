package org.atlas.services.product.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.port.out.repository.BrandRepository;
import org.atlas.services.product.domain.entity.Brand;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaBrand;
import org.atlas.services.product.infrastructure.persistence.jpa.mapper.JpaBrandMapper;
import org.atlas.services.product.infrastructure.persistence.jpa.repository.JpaBrandRepository;
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
