package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.ObjectMapperUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCreateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Integer handle(ProductEntity product) throws Exception {
    // Insert product into DB
    productRepository.insert(product);

    // Upload image
    productImageService.uploadImage(product.getId(), product.getImage());

    // Publish event
    publishEvent(product);

    // Return inserted ID
    return product.getId();
  }

  private void publishEvent(ProductEntity product) {
    Product productPayload = ObjectMapperUtil.getInstance().map(product, Product.class);
    ProductCreatedEvent event = new ProductCreatedEvent(productPayload);
    productEventMessagePublisher.publish(event);
  }
}
