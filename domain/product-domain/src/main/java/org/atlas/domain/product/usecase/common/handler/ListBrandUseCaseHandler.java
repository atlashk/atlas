package org.atlas.domain.product.usecase.common.handler;

import java.util.List;

import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.repository.BrandRepository;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;

import lombok.RequiredArgsConstructor;

@UseCaseHandler
@RequiredArgsConstructor
public class ListBrandUseCaseHandler {

  private final BrandRepository brandRepository;

  public List<BrandEntity> handle(Void input) throws Exception {
    return brandRepository.findAll();
  }
}
