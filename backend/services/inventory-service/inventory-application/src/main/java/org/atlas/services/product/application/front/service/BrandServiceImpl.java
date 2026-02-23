package org.atlas.services.product.application.front.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.services.inventory.domain.entity.BrandEntity;
import org.atlas.services.product.port.in.service.BrandService;
import org.atlas.services.product.port.out.repository.BrandRepository;
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
