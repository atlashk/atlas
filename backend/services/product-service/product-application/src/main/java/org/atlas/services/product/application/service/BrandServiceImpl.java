package org.atlas.services.product.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.services.product.application.port.repository.BrandRepository;
import org.atlas.services.product.domain.entity.Brand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

  private final BrandRepository brandRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Brand> retrieveAllBrand() {
    return brandRepository.findAll();
  }
}
