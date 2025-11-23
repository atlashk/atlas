package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.event.mapper.ProductEventMapper;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminDeleteProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Void handle(Integer productId) throws Exception {
    // Delete productPayload from DB
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    productRepository.delete(product.getId());

    // Publish event
    publishEvent(product);

    return null;
  }

  private void publishEvent(Product product) {
    ProductEvent event = new ProductEvent(DomainEventType.PRODUCT_DELETED);
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
  }
}
