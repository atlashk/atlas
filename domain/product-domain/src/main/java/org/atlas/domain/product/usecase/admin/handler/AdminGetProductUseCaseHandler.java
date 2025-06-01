package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminGetProductUseCaseHandler {

  private final ProductRepository productRepository;

  public ProductEntity handle(Integer productId) throws Exception {
    return productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));
  }
}
