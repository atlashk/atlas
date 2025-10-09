package org.atlas.domain.product.usecase.front.handler;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.kv.KvConfig;
import org.atlas.framework.kv.KvService;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontGetProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final KvService kvService;
  private final KvConfig kvConfig;

  public ProductEntity handle(Integer productId) throws Exception {
    // Get from cache first
    return kvService.get(kvConfig.getProductStoreName(), String.valueOf(productId))
        .map(ProductEntity.class::cast)
        .orElseGet(() -> {
          // Get from DB
          ProductEntity product = productRepository.findById(productId)
              .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

          // Set image
          product.setImage(productImageService.getImage(product.getId()));

          // Update cache
          kvService.put(kvConfig.getProductStoreName(), String.valueOf(productId), product,
              Duration.ofHours(1));

          return product;
        });
  }
}
