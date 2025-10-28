package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.framework.cache.ApplicationCache;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.contract.product.ProductUpdatedEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.StringUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminUpdateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final CacheService cacheService;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Void handle(Product product) throws Exception {
    // Find product
    Product existingProduct = productRepository.findById(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Update product into DB
    AdminProductMapper.INSTANCE.merge(product, existingProduct);
    productRepository.update(product);

    // Upload image
    if (StringUtil.isBlank(product.getImage())) {
      productImageService.uploadImage(product.getId(), product.getImage());
    }

    // Evict cache
    cacheService.evict(ApplicationCache.PRODUCT, String.valueOf(product.getId()));

    // Publish event
    publishEvent(product);

    return null;
  }

  private void publishEvent(Product product) {
    org.atlas.framework.domain.event.contract.product.model.Product productPayload =
        AdminProductMapper.INSTANCE.toProduct(product);
    ProductUpdatedEvent event = new ProductUpdatedEvent(productPayload);
    productEventMessagePublisher.publish(event);
  }
}
