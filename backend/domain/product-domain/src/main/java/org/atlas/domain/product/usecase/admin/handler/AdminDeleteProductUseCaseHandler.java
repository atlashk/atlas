package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.contract.product.ProductDeletedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminDeleteProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Void handle(Integer productId) throws Exception {
    // Delete product from DB
    ProductEntity product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    productRepository.delete(product.getId());

    // Delete image
    productImageService.deleteImage(product.getId());

    // Publish event
    publishEvent(product);

    return null;
  }

  private void publishEvent(ProductEntity product) {
    Product productPayload = ObjectMapperUtil.getInstance().map(product, Product.class);
    ProductDeletedEvent event = new ProductDeletedEvent(productPayload);
    productEventMessagePublisher.publish(event);
  }
}
