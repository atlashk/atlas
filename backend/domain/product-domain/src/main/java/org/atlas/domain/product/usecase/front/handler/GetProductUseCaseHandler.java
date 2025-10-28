package org.atlas.domain.product.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.cache.Cache;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class GetProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  @Cache(cacheName = "product", key = "#productId", ttl = 3600)
  public Product handle(Integer productId) throws Exception {
    // Get from DB
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Set image
    product.setImage(productImageService.getImage(product.getId()));

    return product;
  }
}
