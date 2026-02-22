package org.atlas.services.catalog.application.brand.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.port.in.brand.service.BrandService;
import org.atlas.services.catalog.port.out.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

  private final BrandRepository brandRepository;

  @Override
  @Transactional(readOnly = true)
  public List<BrandEntity> retrieveAllBrand() {
    return brandRepository.findAll();
  }
}
