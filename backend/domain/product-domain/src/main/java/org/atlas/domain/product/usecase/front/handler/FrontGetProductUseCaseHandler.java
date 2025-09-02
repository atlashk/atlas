package org.atlas.domain.product.usecase.front.handler;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.kv.KvConfig;
import org.atlas.framework.kv.KvPort;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontGetProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final KvPort kvPort;
  private final KvConfig kvConfig;

  public ProductEntity handle(Integer productId) throws Exception {
    // Get from cache first
    return kvPort.get(kvConfig.getProductStoreName(), String.valueOf(productId))
        .map(ProductEntity.class::cast)
        .orElseGet(() -> {
          // Get from DB
          ProductEntity productEntity = productRepository.findById(productId)
              .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));

          // Set image
          productEntity.setImage(productImageService.getImage(productEntity.getId()));

          // Update cache
          kvPort.put(kvConfig.getProductStoreName(), String.valueOf(productId), productEntity,
              Duration.ofHours(1));

          return productEntity;
        });
  }
}
