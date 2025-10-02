package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCreateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ApplicationConfigPort applicationConfigPort;
  private final MessagePublisherPort messagePublisherPort;

  public Integer handle(ProductEntity productEntity) throws Exception {
    // Insert product into DB
    productRepository.insert(productEntity);

    // Upload image
    productImageService.uploadImage(productEntity.getId(), productEntity.getImage());

    // Publish event
    publishEvent(productEntity);

    // Return inserted ID
    return productEntity.getId();
  }

  private void publishEvent(ProductEntity productEntity) {
    Product product = ObjectMapperUtil.getInstance().map(productEntity, Product.class);
    ProductCreatedEvent event = new ProductCreatedEvent(applicationConfigPort.getApplicationName(),
        product);
    messagePublisherPort.publish(event);
  }
}
