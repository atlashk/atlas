package org.atlas.domain.product.usecase.front.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Brand;
import org.atlas.domain.product.repository.BrandRepository;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class ListBrandUseCaseHandler {

  private final BrandRepository brandRepository;

  public List<Brand> handle(Void input) throws Exception {
    return brandRepository.findAll();
  }
}
