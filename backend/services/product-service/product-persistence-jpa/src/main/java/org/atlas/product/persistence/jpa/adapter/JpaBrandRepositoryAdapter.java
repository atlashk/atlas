package org.atlas.product.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.product.application.port.repository.BrandRepository;
import org.atlas.product.domain.entity.Brand;
import org.atlas.common.framework.util.ObjectMapperUtil;
import org.atlas.product.persistence.jpa.entity.JpaBrand;
import org.atlas.product.persistence.jpa.mapper.JpaBrandMapper;
import org.atlas.product.persistence.jpa.repository.JpaBrandRepository;
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
