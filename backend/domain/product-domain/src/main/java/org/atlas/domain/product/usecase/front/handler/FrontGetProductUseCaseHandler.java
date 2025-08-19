package org.atlas.domain.product.usecase.front.handler;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.cache.CachePort;
import org.atlas.framework.config.Application;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontGetProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ApplicationConfigPort applicationConfigPort;
  private final CachePort cachePort;

  public ProductEntity handle(Integer productId) throws Exception {
    String cacheName = Optional.ofNullable(
            applicationConfigPort.getConfig(Application.PRODUCT_SERVICE, "product-cache-name"))
        .orElseThrow(() -> new IllegalStateException("product-cache-name is not configured"));
    String cacheKey = String.valueOf(productId);

    // Get from cache first
    return cachePort.get(cacheName, cacheKey)
        .map(ProductEntity.class::cast)
        .orElseGet(() -> {
          // Get from DB
          ProductEntity productEntity = productRepository.findById(productId)
              .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));

          // Set image
          productEntity.setImage(productImageService.getImage(productEntity.getId()));

          // Update cache
          cachePort.set(cacheName, cacheKey, productEntity, Duration.ofHours(1));

          return productEntity;
        });
  }
}
