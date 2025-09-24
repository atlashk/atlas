package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCountProductUseCaseHandler {

  private final ProductRepository productRepository;

  public Long handle() throws Exception {
    return productRepository.countAll();
  }
}
