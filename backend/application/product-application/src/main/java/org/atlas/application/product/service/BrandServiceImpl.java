package org.atlas.application.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.port.repository.BrandRepository;
import org.atlas.domain.product.entity.Brand;
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
