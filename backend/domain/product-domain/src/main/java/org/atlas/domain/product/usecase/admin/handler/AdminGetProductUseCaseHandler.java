package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminGetProductUseCaseHandler {

  private final ProductRepository productRepository;

  public Product handle(Integer productId) throws Exception {
    return productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
  }
}
