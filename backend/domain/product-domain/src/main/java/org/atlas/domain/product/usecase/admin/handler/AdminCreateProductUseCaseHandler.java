package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCreateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Integer handle(Product product) throws Exception {
    // Insert product into DB
    productRepository.insert(product);

    // Upload image
    productImageService.uploadImage(product.getId(), product.getImage());

    // Publish event
    publishEvent(product);

    // Return inserted ID
    return product.getId();
  }

  private void publishEvent(Product product) {
    org.atlas.framework.domain.event.contract.product.model.Product productPayload =
        AdminProductMapper.INSTANCE.toProduct(product);
    ProductCreatedEvent event = new ProductCreatedEvent(productPayload);
    productEventMessagePublisher.publish(event);
  }
}
