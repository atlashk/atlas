package org.atlas.infrastructure.persistence.jpa.impl.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.repository.BrandRepository;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaBrand;
import org.atlas.infrastructure.persistence.jpa.impl.product.mapper.JpaBrandMapper;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.JpaBrandRepository;
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
